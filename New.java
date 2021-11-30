/**
 * Example of Spring 4 Properties Java Configuration,
 *  with a Database Properties table to store most values
 *  and a small application.properties file too.
 * The Database table will take precedence over the properties file with this setup
 */
@Configuration
@PropertySource(value = { "classpath:application.properties" }, ignoreResourceNotFound=true)
public class SpringPropertiesConfig {
    private static final Logger log = LoggerFactory.getLogger(SpringPropertiesConfig.class);

    @Inject
    private org.springframework.core.env.Environment env;

    @PostConstruct
    public void initializeDatabasePropertySourceUsage() {
        MutablePropertySources propertySources = ((ConfigurableEnvironment) env).getPropertySources();

        try {
            //dataSource, Table Name, Key Column, Value Column
            DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(dataSource(), "AppConfiguration", "propertyKey", "propertyValue");
            
            //CommonsConfigurationFactoryBean comes from https://java.net/projects/springmodules/sources/svn/content/tags/release-0_8/projects/commons/src/java/org/springmodules/commons/configuration/CommonsConfigurationFactoryBean.java?rev=2110
            //Per https://jira.spring.io/browse/SPR-10213 I chose to just copy the raw source into our project
            CommonsConfigurationFactoryBean commonsConfigurationFactoryBean = new CommonsConfigurationFactoryBean(databaseConfiguration);
            
            Properties dbProps = (Properties) commonsConfigurationFactoryBean.getObject();
            PropertiesPropertySource dbPropertySource = new PropertiesPropertySource("dbPropertySource", dbProps);

            //By being First, Database Properties take precedence over all other properties that have the same key name
            //You could put this last, or just in front of the application.properties if you wanted to...
            propertySources.addFirst(dbPropertySource);
        } catch (Exception e) {
            log.error("Error during database properties setup", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Read why this is required: http://www.baeldung.com/2012/02/06/properties-with-spring/#java
     * It is important to be static: http://www.java-allandsundry.com/2013/07/spring-bean-and-propertyplaceholderconf.html
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    
    @Bean
    public DataSource dataSource()
    {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
        dataSource.setUrl(env.getProperty("jdbc.url"));
        dataSource.setUsername(env.getProperty("jdbc.username"));
        dataSource.setPassword(env.getProperty("jdbc.password"));
        return dataSource;
    }
    
}