# pdfserver
Small module to help generating PDFs in a Spring MVC webapp

## Usage

Add the following to your pom:

    <dependency>
        <groupId>org.vermillionalbatros</groupId>
        <artifactId>pdfserver</artifactId>
        <version>0.0.1</version>
    </dependency>
	
Configure Spring MVC with the following bean:

  @Configuration
  public class WebMvcConfig extends WebMvcConfigurerAdapter {
      
      @Override
      public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
          configurer.favorPathExtension(true)
              .favorParameter(false)
              .ignoreAcceptHeader(true)
              .useJaf(false)
              .defaultContentType(MediaType.APPLICATION_JSON)
              .mediaType("pdf", PdfResponseConverter.PDF_MEDIA)
              ;
      }
      
      @Bean
      public PdfResponseConverter pdfConverter() {
          PdfResponseConverter converter = new PdfResponseConverter();
          converter.setAlwaysReload(true);
          converter.setContentFormatter(content -> content.replaceAll("ő", "ö").replaceAll("Ő", "Ö"));
          return converter;
      }
      
      @Bean
      public HtmlMessageConverter htmlConverter() {
          return new HtmlMessageConverter();
      }
  }

	
