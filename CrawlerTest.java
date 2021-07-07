package crawler.application;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;


public class CrawlerTest {
  
  private static final String CNPJ = "cnpj";
  private static final String EMAIL = "email";
  private static final String RAZAO_SOCIAL = "razao_social";

  @Test
  
  @Ignore
  public void shouldFindART() throws Throwable {
    OlxCrawlerService consumer = new OlxCrawlerService(30000, 60000);
      List<String> payloads =  consumer.harvestEvent(ImmutableMap.<String, String> builder().put(CNPJ, "18033552000161").put(RAZAO_SOCIAL, "99Pop").build());
      for (String payload : payloads) {
        System.out.println(payload);
      }
  }
  
  @Test
  public void shouldParse() throws Throwable {
    OlxCrawlerService consumer = new OlxCrawlerService(30000, 60000);
    List<String> payloads =  consumer.harvestEvent(ImmutableMap.<String, String> builder().put(CNPJ, "70402284000128").put(RAZAO_SOCIAL, "CONDOMINIO CASA GRANDE").build());
        
        
        boolean cadastrada = false;
        if(payloads.get(0).contains("Informamos que esse CNPJ já consta em nossa base de dados.")) {
          cadastrada = true;
        }
        if(payloads.get(0).contains("Já existe um usuário com esta identificação")) {
          cadastrada = true;
        }
        System.out.println(cadastrada);
        

  }

  @Test(expected = IllegalArgumentException.class)
  @Ignore
  public void shouldNotParseARTWithoutQtd() throws Throwable {
    OlxCrawlerService consumer = new OlxCrawlerService(30000, 60000);
    List<String> payloads =  consumer.harvestEvent(ImmutableMap.<String, String> builder().put(CNPJ, "18.033.552/0001-61").put(EMAIL, "peced74237@gameqo.com").put(RAZAO_SOCIAL, "99Pop").build());
      for (String payload : payloads) {
        System.out.println(payload);
      }
  }
}