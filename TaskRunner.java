package crawler.runner;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;

import crawler.application.OlxCrawlerService;
import infraestructure.DataUnavailableException;
import infraestructure.SpecialErrorException;
import infraestructure.NotFoundException;
import infraestructure.RequestStatus;
import infraestructure.RetryException;
import infraestructure.SiteUnavailableException;
import infraestructure.UnknownErrorException;
import infraestructure.WrongCaptchaException;
import crawler.repository.SintegraRepository;

@Named
@Singleton
public class TaskRunner {

  private final OlxCrawlerService sintegraCrawlerService;
  private final SintegraRepository sintegraRepository;
  private final Integer runnersQuantity;
  private final Integer sleepTime;

  @Inject
  public TaskRunner(OlxCrawlerService sintegraCrawlerService,  SintegraRepository sintegraRepository, @Value("${crawler.runners}") Integer runnersQuantity, @Value("${crawler.sleep.seconds}") Integer sleepTime) {
    this.sintegraCrawlerService = sintegraCrawlerService;
    this.sintegraRepository = sintegraRepository;
    this.runnersQuantity = runnersQuantity;
    this.sleepTime = sleepTime;
  }

  @PostConstruct
  public void initConsumers() {
    ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(this.runnersQuantity);

    for (int i = 0; i < this.runnersQuantity; i++)
      this.initConsumer(threadPool);
  }

  private void initConsumer(ScheduledExecutorService threadPool) {
    Runnable consumer = new BotTaskRunner(sintegraCrawlerService, sintegraRepository, sleepTime);
    threadPool.schedule(consumer, 1, TimeUnit.SECONDS);
  }
}

class BotTaskRunner implements Runnable {

  private final OlxCrawlerService sintegraCrawlerService;
  private final SintegraRepository sintegraRepository;
  private final Logger logger;
  private final Integer sleepTime;

  public BotTaskRunner(OlxCrawlerService detranCrawlerService, SintegraRepository detranRepository, Integer sleepTime) {
    this.sintegraCrawlerService = detranCrawlerService;
    this.sintegraRepository = detranRepository;
    this.sleepTime = sleepTime;
    this.logger = LoggerFactory.getLogger(this.getClass());
  }

  @Override
	public void run() {
		while (true) {
			try {
				Optional<List<Map<String, String>>> chassis = this.sintegraRepository.gatherInput(5);

				if (!chassis.isPresent()) {
					this.logger.info("WAITING FOR INPUT DATA - SLEEPING {} SECONDS", sleepTime);
					TimeUnit.SECONDS.sleep(sleepTime);
					continue;
				}
				for (Map<String, String> input : chassis.get()) {
					List<String> payloads = new ArrayList<String>();
					this.logger.info("Harvesting INPUT: {}", input);
					try {
						payloads = this.sintegraCrawlerService.harvestEvent(input);
						if (payloads.isEmpty()) {
							this.logger.error("NAO DEVERIA CHEGAR AQUI - CRAWLER SERVICE RETORNANDO LISTA VAZIA");
							System.exit(-1);
						}
					} catch (Throwable ex) {
						RequestStatus status = null;
						if (ex instanceof NotFoundException) {
							this.sintegraRepository.updateInput(input, status = RequestStatus.NOT_FOUND);
						} else if (ex instanceof WrongCaptchaException) {
							this.sintegraRepository.updateInput(input, status = RequestStatus.RETRY);
						} else if (ex instanceof DataUnavailableException) {
							this.sintegraRepository.updateInput(input, status = RequestStatus.DATA_UNAVAILABLE);
							this.logger.info("DATA UNAVAILABLE");
						} else if (ex instanceof SocketTimeoutException) {
							this.logger.info("SOCKET TIME OUT");
							this.sintegraRepository.updateInput(input, status = RequestStatus.RETRY);
						} else if (ex instanceof RetryException) {
							this.sintegraRepository.updateInput(input, status = RequestStatus.RETRY);
						} else if (ex instanceof ConnectTimeoutException) {
							this.logger.info("CONNECT TIME OUT");
							this.sintegraRepository.updateInput(input, status = RequestStatus.RETRY);
						} else if (ex instanceof SpecialErrorException) {
							this.sintegraRepository.updateInput(input, status = RequestStatus.SPECIAL_ERROR);
						} else if (ex instanceof SiteUnavailableException) {
							this.sintegraRepository.updateInput(input, status = RequestStatus.SITE_UNAVAILABLE);
							this.logger.info("SITE UNAVAILABLE - SLEEPING {} SECONDS", sleepTime);
							TimeUnit.SECONDS.sleep(60);
							continue;
						} else if (ex instanceof UnknownErrorException) { 
							this.sintegraRepository.updateInput(input, status = RequestStatus.UNKNOWN_ERROR);
							this.logger.error("Unknown Content {}", ((UnknownErrorException) ex).getPayload() );
						} else if (ex instanceof IllegalArgumentException) {
							this.sintegraRepository.updateInput(input, status = RequestStatus.PARSER_EXCEPTION);
						} else if (ex instanceof IOException) {
							this.sintegraRepository.updateInput(input, status = RequestStatus.PROXY_ERROR);
						} else if (ex instanceof DataAccessException) {
							this.logger.error("Input {} ERROR ON DB {}", input, ex.getMessage());
							System.exit(-1);
						} else {
							this.logger.error("Unkown Error", ex);    
							if (status == null) status = RequestStatus.UNKNOWN_ERROR;
							this.sintegraRepository.updateInput(input, status);
						}
					}
			      
						if(payloads.get(0).contains("Informamos que esse CNPJ já consta em nossa base de dados.")) {
						  this.sintegraRepository.save(input, true, "Consta na base de dados");
						}else if(payloads.get(0).contains("Já existe um usuário com esta identificação")) {
						  this.sintegraRepository.save(input, true, "Cadastrado");
						}else {
						  this.sintegraRepository.save(input, false, "Não Consta / Não Cadastrado");
						  
						}
						continue;
				}
			} catch (DataAccessException e) {
				this.logger.error("ERRO NO BANCO {}", e.getMessage());
				System.exit(-1);
			} catch (Throwable e) {
				this.logger.error(e.getMessage());
			}
		}
	}
}
