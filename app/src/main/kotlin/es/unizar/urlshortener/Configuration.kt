package es.unizar.urlshortener

import GenerateQRUseCaseImpl
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCaseImpl
import es.unizar.urlshortener.core.usecases.InfoHTTPHeaderCaseImpl
import es.unizar.urlshortener.core.usecases.LogClickUseCaseImpl
import es.unizar.urlshortener.core.usecases.RedirectUseCaseImpl
import es.unizar.urlshortener.infrastructure.delivery.*
import es.unizar.urlshortener.infrastructure.repositories.ClickEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.ClickRepositoryServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlRepositoryServiceImpl
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


/**
 * Wires use cases with service implementations, and services implementations with repositories.
 *
 * **Note**: Spring Boot is able to discover this [Configuration] without further configuration.
 */
@Configuration
class ApplicationConfiguration(
    @Autowired val shortUrlEntityRepository: ShortUrlEntityRepository,
    @Autowired val clickEntityRepository: ClickEntityRepository,
    @Autowired val rabbitTemplate: RabbitTemplate,


    ) {
    @Bean
    fun clickRepositoryService() = ClickRepositoryServiceImpl(clickEntityRepository)

    @Bean
    fun myQueue1(): Queue? {
        return Queue("safe", false)
    }

    @Bean
    fun myQueue2(): Queue? {
        return Queue("csv", false)
    }

    @Bean
    fun exchange(): TopicExchange? {
        return TopicExchange("exchange")
    }

    @Bean
    fun bindingSafeBrowsing(@Qualifier("myQueue1") queue: Queue?, exchange: TopicExchange?): Binding? {
        return BindingBuilder.bind(queue).to(exchange).with("safe")
    }
    @Bean
    fun bindingCSV(@Qualifier("myQueue2") queue: Queue?, exchange: TopicExchange?): Binding? {
        return BindingBuilder.bind(queue).to(exchange).with("csv")
    }

    @Bean
    fun shortUrlRepositoryService() = ShortUrlRepositoryServiceImpl(shortUrlEntityRepository)

    @Bean
    fun validatorService() = ValidatorServiceImpl()

    @Bean
    fun rabbitMQService() = RabbitMQServiceImpl(rabbitTemplate,shortUrlRepositoryService())

    @Bean
    fun hashService() = HashServiceImpl()

    @Bean
    fun QRService() = QRServiceImpl()

    @Bean
    fun redirectUseCase() = RedirectUseCaseImpl(shortUrlRepositoryService())

    @Bean
    fun logClickUseCase() = LogClickUseCaseImpl(clickRepositoryService())



    @Bean
    fun googleSafeBrowsing() = GoogleSafeBrowsingServiceImpl()


    @Bean
    fun createShortUrlUseCase() =
        CreateShortUrlUseCaseImpl(shortUrlRepositoryService(), validatorService(), hashService(), rabbitMQService())

    @Bean
    fun generateQRUseCase() = GenerateQRUseCaseImpl(shortUrlRepositoryService(), QRService())


    @Bean
    fun infoHTTPHeaderUserCase() = InfoHTTPHeaderCaseImpl(clickRepositoryService(),shortUrlRepositoryService())

}