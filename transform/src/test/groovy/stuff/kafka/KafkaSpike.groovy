package stuff.kafka


import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import spock.lang.Specification

import java.time.Instant

class KafkaSpike extends Specification {
    def 'send something'() {
        when:
        def client = new ProducerClient<>('localhost:9092', 'test-client', Serdes.String().serializer())

        client.init()
        10.times {
            client.send('testtopic', 'm1', 'hi there ' + it)
        }

        then:
        sleep(5000)
    }

    def 'send filebeat data'() {
        def random = Random.newInstance()

        when:
        def client = new ProducerClient<>('kafka-0.kafka:9092',
                'filebeat-1', KafkaConsts.JSON_SE)
        client.init()

        def start = Instant.now()
        100.times {
            def log = new FilebeatEntity(
                    timestamp: new Date(),
                    message: 'Started by user [8mha:////blablub[0mtestuser',
                    fields: new FilebeatEntity.Fields(
                            master: 'test-jenkins',
                            protocol: 'https',
                            domainSuffix: 'streams.nowhere'
                    ),
                    log: new FilebeatEntity.Log(offset: 100 * it,
                            file: new FilebeatEntity.Log.File(path: "/var/jenkins/jobs/testbuild/jobs/${start.toString()}/builds/${it}/log"))
            )
            println it
            sleep(150 + random.nextInt(200))

            def log2 = new FilebeatEntity(
                    timestamp: new Date(),
                    message: 'hi there',
                    fields: new FilebeatEntity.Fields(
                            master: 'test-jenkins',
                            protocol: 'https',
                            domainSuffix: 'streams.nowhere'
                    ),
                    log: new FilebeatEntity.Log(offset: 100 * it,
                            file: new FilebeatEntity.Log.File(path: "/var/jenkins/jobs/testbuild/jobs/${start.toString()}/builds/${it}/log"))
            )

            sleep(350 + random.nextInt(300))

            def log3 = new FilebeatEntity(
                    timestamp: new Date(),
                    message: 'Finished: ' + ['SUCCESS', 'FAILURE', 'ABORTED', 'UNSTABLE', 'NOT_BUILT'][random.nextInt(5)],
                    fields: new FilebeatEntity.Fields(
                            master: 'test-jenkins',
                            protocol: 'https',
                            domainSuffix: 'streams.nowhere'
                    ),
                    log: new FilebeatEntity.Log(offset: 100 * it,
                            file: new FilebeatEntity.Log.File(path: "/var/jenkins/jobs/testbuild/jobs/${start.toString()}/builds/${it}/log"))
            )

            client.send('filebeat-1', null, log)
            client.send('filebeat-1', null, log2)
            client.send('filebeat-1', null, log3)

            sleep(250)
        }

        then:
        sleep(1000)
    }

    def 'send durations'() {
        def random = Random.newInstance()

        when:
        def client = new ProducerClient<>('kafka-0.kafka:9092',
                'filebeattest1', KafkaConsts.JSON_SE)
        client.init()

        def begin = Instant.now()

        1.times {
            client.send('filebeat-2', null,
                    DummyEvents.createLog("${begin}-$it", it, Instant.now(), random.nextInt(400000) + 1000, 'hi there'))
            sleep(random.nextInt(40) + 10)
            client.send('filebeat-2', null,
                    DummyEvents.createStart("${begin}-$it", it, Instant.now(), random.nextInt(400000) + 1000))
            sleep(random.nextInt(400) + 50)
            client.send('filebeat-2', null,
                    DummyEvents.createEnd("${begin}-$it", it, Instant.now(), random.nextInt(400000) + 1000))
        }

        then:
        true
    }

    def 'push real jenkins log'() {
        def lines = new File('jenkinsbuild.log').readLines()

        def client = new ProducerClient<>('kafka-0.kafka:9092',
                'filebeat-1', KafkaConsts.JSON_SE)
        client.init()

        def start = Instant.now()

        lines.eachWithIndex { line, n ->
            def log = new FilebeatEntity(
                    timestamp: new Date(),
                    message: line,
                    fields: new FilebeatEntity.Fields(
                            master: 'test-jenkins',
                            protocol: 'https',
                            domainSuffix: 'streams.nowhere'
                    ),
                    log: new FilebeatEntity.Log(offset: 100 * n,
                            file: new FilebeatEntity.Log.File(path: "/var/jenkins/jobs/testbuild-real-log/jobs/${start.toString()}/builds/${1}/log"))
            )

            sleep(100)

            client.send('filebeat-1', null, log)
        }

        expect:
        true
    }

    def 'print filebeat entity'() {
        expect:
        println KafkaConsts.JSON.writeValueAsString(new FilebeatEntity(
                timestamp: new Date(),
                message: 'hey',
                fields: new FilebeatEntity.Fields(
                        master: 'test-jenkins',
                        protocol: 'https',
                        domainSuffix: 'streams.nowhere'
                ),
        ))
    }

    def 'do some streams stuff'() {
        when:
        println 'test'

        new StreamsRunner('kafka-0.kafka:9092', 'testapp', 'latest') {
            @Override
            StreamsBuilder createTopologyBuilder() {
                def builder = new StreamsBuilder()
                KStream<String, String> stream = builder.stream('testtopic', Consumed.with(Serdes.String(), Serdes.String()))

                stream
                        .peek { key, value -> println value }
                        .to('someothertopic')

                builder
            }
        }.start()

        println 'active'

        sleep(1000000)

        then:
        true
    }

    def 'do some json streams stuff'() {
        when:
        println 'test'

        new StreamsRunner('kafka-0.kafka:9092', 'testapp-json', 'earliest') {
            @Override
            StreamsBuilder createTopologyBuilder() {
                def builder = new StreamsBuilder()
                KStream<String, String> stream = builder.stream('logevent-1',
                        Consumed.with(Serdes.String(), KafkaConsts.createJsonSerde(Map)))

                stream
                        .peek { key, value -> println value }
//                        .to('someothertopic')

                builder
            }
        }.start()

        println 'active'

        sleep(1000000)

        then:
        true
    }

    def 'build tf'() {
        when:
        def buildTf = new BuildTf('kafka-0.kafka:9092', 'build-agg', 'latest')
        buildTf.start()

        then:
        sleep(600000)

    }

    def 'build tf cli'() {
        when:
        SimpleApp.main('buildTf', 'kafka-0.kafka:9092', 'build-agg', 'latest', 'logevent-1', 'build-1')

        then:
        sleep(600000)
    }

    def 'duration tf cli'() {
        when:
        SimpleApp.main('durationTf', 'kafka-0.kafka:9092', 'duration-agg-test', 'latest', 'logevent-1', 'duration-2')

        then:
        sleep(6000000)
    }

    def 'poll something'() {
        when:
        def poll = new LoopClient<>('localhost:9092', 'poll-client')

        poll.init('testtopic', { records ->
            records.forEach {
                println it.value()
            }
        })

        then:
        sleep(60000)
    }
}
