package stuff.kafka

class SimpleApp {
    static void main(String[] args) {
        if (args[0] == 'buildTf') {
            BuildTf.create(args.drop(1))
        } else {
            println 'hi there, sleeping for 10min'
            sleep(600000)
            println 'done sleeping'
        }
    }
}
