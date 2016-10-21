(require '[arachne.core.dsl :as core])

(core/runtime :test/runtime [:test/component-a])

;; We need at least one component, so we'll just use a simple map
(core/component :test/component-a {}
  'clojure.core/hash-map)
