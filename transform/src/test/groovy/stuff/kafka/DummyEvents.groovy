package stuff.kafka

import java.time.Instant

class DummyEvents {
    static FilebeatEntity createStart(String jobName, int buildNum, Instant time, int offset) {
        new FilebeatEntity(
                timestamp: Date.from(time),
                message: "$time|duration|start|test",
                fields: new FilebeatEntity.Fields(
                        master: 'test-jenkins',
                        protocol: 'https',
                        domainSuffix: 'streams.nowhere'
                ),
                log: new FilebeatEntity.Log(offset: offset,
                        file: new FilebeatEntity.Log.File(path: "/var/jenkins/jobs/testbuild/jobs/${jobName}/builds/${buildNum}/log"))
        )
    }

    static FilebeatEntity createEnd(String jobName, int buildNum, Instant time, int offset) {
        new FilebeatEntity(
                timestamp: Date.from(time),
                message: "$time|duration|end|test",
                fields: new FilebeatEntity.Fields(
                        master: 'test-jenkins',
                        protocol: 'https',
                        domainSuffix: 'streams.nowhere'
                ),
                log: new FilebeatEntity.Log(offset: offset,
                        file: new FilebeatEntity.Log.File(path: "/var/jenkins/jobs/testbuild/jobs/${jobName}/builds/${buildNum}/log"))
        )
    }

    static FilebeatEntity createLog(String jobName, int buildNum, Instant time, int offset, String message) {
        new FilebeatEntity(
                timestamp: Date.from(time),
                message: message,
                fields: new FilebeatEntity.Fields(
                        master: 'test-jenkins',
                        protocol: 'https',
                        domainSuffix: 'streams.nowhere'
                ),
                log: new FilebeatEntity.Log(offset: offset,
                        file: new FilebeatEntity.Log.File(path: "/var/jenkins/jobs/testbuild/jobs/${jobName}/builds/${buildNum}/log"))
        )
    }
}
