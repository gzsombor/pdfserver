![Maven Build](https://github.com/gzsombor/pdfserver/workflows/Maven%20Build/badge.svg)

# pdfserver
Small module to help generating PDFs in a Spring MVC webapp

## Usage

Add a new repository to your pom:

    <repositories>
      <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/gzsombor/pdfserver</url>
      </repository>
    </repositories>


Add this dependency to your pom:

    <dependency>
        <groupId>com.github.gzsombor</groupId>
        <artifactId>pdfserver</artifactId>
        <version>v0.8.7</version>
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
            // always force template reloading
            converter.setAlwaysReload(true);
            // to filter out some not displayable characters in PDF
            converter.setContentFormatter(content -> content.replaceAll("ő", "ö").replaceAll("Ő", "Ö"));
            converter.setPathPrefix("pdf/");
            return converter;
        }
        
        @Bean
        public HtmlMessageConverter htmlConverter() {
            HtmlMessageConverter converter = new HtmlMessageConverter();
            converter.setPathPrefix("pdf/");
            return new HtmlMessageConverter();
        }
    }

Add PdfOutput interface to the DTOs, for which the PDF generation should happen. It needs two method, to return the template name, which is used for rendering, and the name of the generated file name, for example: 

    @Override
    @JsonIgnore
    public String getTemplateName() {
        return "myReportTemplate";
    }

    @Override
    @JsonIgnore
    public String getOutputName() {
        return "report-"+date.toString();
    }

In this case, it will use template from the classpath, under pdf/myReportTemplate.html, and the generated report could be 'report-2015-11-20.pdf'.

And finally, in a Spring REST Controller: 

    @RestController
    @RequestMapping("/api")
    public class MyReportResource {
    
            @RequestMapping(value = "/report/{id}",method = RequestMethod.GET)
            public MyDTO get(@PathVariable Long id) {
                return new MyDTO();
            }

In this case, when /api/report/123.html called, then the html report is generated, and when the /api/report/123.pdf then the PDF


