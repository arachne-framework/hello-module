(require '[arachne.core.dsl :as core])
(require '[arachne.hello :as hello])

(core/runtime :test/runtime [:test.greeting/spanish :test.greeting/informal])

(hello/greeter :test.greeting/spanish "Hola!")
(hello/greeter :test.greeting/informal "Hi!")