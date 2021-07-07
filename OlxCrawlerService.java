package crawler.application;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.springframework.beans.factory.annotation.Value;

import infraestructure.HttpGatewayImpl;
import infraestructure.HttpMethod;
import infraestructure.HttpResponse;
import infraestructure.ProxyException;
import infraestructure.UnknownErrorException;
import crawler.repository.SessionRepository;
import crawler.util.StringRegexUtil;

@Named
public class OlxCrawlerService {

  private final Integer socketTimeout;
  private final Integer connectionTimeout;

  @Inject
  public OlxCrawlerService(@Value("${crawler.socket.timeout:60000}") final Integer socketTimeout,
      @Value("${crawler.connection.timeout:120000}") final Integer connectionTimeout) {

    this.socketTimeout = socketTimeout;
    this.connectionTimeout = connectionTimeout;
  }

  public List<String> harvestEvent(final Map<String, String> input) throws Throwable {
    final CookieStore cookies = new BasicCookieStore();
    HttpResponse response = null;

    response = new HttpGatewayImpl("https://www.me.com.br/do/Register.mvc/Create?pais=BW&tipoCadastro=1&plan=0", HttpMethod.GET)
        .cookieStore(cookies)
        .headers(this.generateHomeHeaders())
        .timeToSleepBetweenRequests(0)
        .connectionTimeout(this.connectionTimeout)
        .socketTimeout(this.socketTimeout)
        .execute();
    
    response = new HttpGatewayImpl("https://www.me.com.br/do/Register.mvc/Create"+ "", HttpMethod.POST)
        .cookieStore(cookies)
        .followRedirect(true)
        .headers(this.generateHeaders())
        .requestParameters(this.generateRequestParams(input))
        .charset(StandardCharsets.ISO_8859_1)
        .timeToSleepBetweenRequests(0)
        .connectionTimeout(this.connectionTimeout)
        .socketTimeout(this.socketTimeout)
        .execute();
    
    String payload = StringEscapeUtils.unescapeHtml4(response.getContent());
    this.validateResponse(response, payload);

    return Arrays.asList(response.getContent());
  }

  private Map<String, String> generateHomeHeaders() {
    final Map<String, String> headers = new HashMap<>();
    headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
    headers.put("accept-encoding", "gzip, deflate, br");
    headers.put("accept-language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7");
    headers.put("cache-control", "max-age=0");
    headers.put("referer", "https://www.me.com.br/do/Register.mvc/Index");
    headers.put("sec-ch-ua", "\"Google Chrome\";v=\"89\", \"Chromium\";v=\"89\", \";Not A Brand\";v=\"99\"");
    headers.put("sec-ch-ua-mobile", "?0");
    headers.put("sec-fetch-dest", "document");
    headers.put("sec-fetch-mode", "navigate");
    headers.put("sec-fetch-site", "same-origin");
    headers.put("sec-fetch-user", "?1");
    headers.put("upgrade-insecure-requests", "1");
    headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
    
    return headers;
  }

  private Map<String, String> generateHeaders() {
    final Map<String, String> headers = new HashMap<>();
    headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
    headers.put("accept-encoding", "gzip, deflate, br");
    headers.put("accept-language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7");
    headers.put("cache-control", "max-age=0");
    headers.put("content-type", "application/x-www-form-urlencoded");
    headers.put("origin", "https://www.me.com.br");
    headers.put("referer", "https://www.me.com.br/do/Register.mvc/Create?pais=BR&tipoCadastro=1&plan=0");
    headers.put("sec-ch-ua", "\"Google Chrome\";v=\"89\", \"Chromium\";v=\"89\", \";Not A Brand\";v=\"99\"");
    headers.put("sec-ch-ua-mobile", "?0");
    headers.put("sec-fetch-dest", "document");
    headers.put("sec-fetch-mode", "navigate");
    headers.put("sec-fetch-site", "same-origin");
    headers.put("sec-fetch-user", "?1");
    headers.put("upgrade-insecure-requests", "1");
    headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
    
    return headers;
  }
  
  
  private Map<String, String> generateRequestParams(final Map<String, String> input) {
    final Map<String, String> params = new HashMap<>();
    params.put("hash", "");
    params.put("TentativaUpgrade", "false");
    params.put("TentativaDowngrade", "false");
    params.put("PlanoComercialAtual", "0");
    params.put("PlanoComercialAtualDescricao", "");
    params.put("PlanoComercialEscolhido", "0");
    params.put("PlanoComercialEscolhidoDescricao", "");
    params.put("AceitePlanoComercial", "");
    params.put("PaisDesc", "Brasil");
    params.put("Pais", "BR");
    params.put("Origem", "1");
    params.put("TipoCadastroDesc", "Pessoa Jurídica");
    params.put("TipoCadastro", "1");
    params.put("LabelCodEmpresa", "CNPJ");
    params.put("CNPJ.Value", input.get("cnpj").replaceAll("\\D", ""));
    params.put("RazaoSocial.Value", input.get("razao_social"));
    params.put("NomeFantasia.Value", "....");
    params.put("IE.Value", "");
    params.put("Endereco", "....");
    params.put("Numero", "1234");
    params.put("CEP.Value", "88032005");
    params.put("UF", "SC");
    params.put("UF2", "");
    params.put("Cidade.Value", "5693e0ae0cf203c4b72a13d8");
    params.put("Municipio", "");
    params.put("Nome", "....");
    params.put("Email", "memero4491@kindbest.com");
    params.put("Telefone", "48991311999");
    params.put("Celular.Value", "");
    params.put("LoginName", "olxolxolx");
    params.put("TipoUser", "1");
   
    return params;
  }
  
  private void validateResponse(final HttpResponse response, final String payload) throws Throwable {
    if (response.getContent() == null)
      throw new ProxyException("Invalid null page content");
    if (response.getHttpStatusCode() != 200)
      throw new ProxyException("Redirected to wrong page");
    if (payload.contains("Informamos que esse CNPJ já consta em nossa base de dados."))
      return;
    if (payload.contains("Já existe um usuário com esta identificação"))
      return;
    if (payload.contains("e680ff"))
      return;
    throw new UnknownErrorException("Não deveria chegar aqui!!", response.getContent());
  }

}
