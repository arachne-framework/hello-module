(ns arachne.hello
  "This namespace contains the entire implementation of the arachne.hello module.

  Usually, the schema, the configuration elements, the DSL and the actual
  implementation would all have their own namespaces, but here we will keep them
  together so it's possible to see all the parts at once."
  (:require [arachne.core.config :as cfg]
            [arachne.core.config.model :as m]
            [arachne.core.config.init :as dsl]
            [arachne.core.dsl.specs :as dslspecs]
            [com.stuartsierra.component :as c]
            [clojure.spec :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; This section is used to build the module's Schema. This function is
;;; specified in `arachne.modules.edn`, and is called by Arachne core during
;;; bootstrap, prior to building a config.

(defn schema
  "Return the schema for the hello module"
  []
  (m/type :arachne.hello/Greeter [:arachne.core/Component]
    "A greeter is a component that prints a greeting to System/out when the system is started."
    (m/attr :arachne.hello.greeter/greeting :one :string
      "The greeting that this greeter will use")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; This section runs during the "module configuration phase". This function is
;;; specified in `arachne-modules.edn`, and is called by Arachne core during
;;; bootstrap, after the user's config initializers have been applied.

(defn- all-runtimes
  "Helper function to find the EIDs of all the runtime entities in a given
  config"
  [cfg]
  (cfg/q cfg '[:find [?rt ...]
               :where
               [?rt :arachne.runtime/components _]]))

(defn configure
  "Configure the hello module. If there are any greeters already present in the
  config (i.e, added explicitly by the user), this is a no-op and just returns
  the config. Otherwise, we:

   - Create txdata for a greeter entity
   - Find all the runtimes that are present in the config
   - For each runtime, append a datom to the txdata that creates a dependency on
     our new greeter entity component.
   - Transact it."
  [cfg]
  (let [existing-greeters (cfg/q cfg '[:find [?g ...]
                              :where
                              [?g :arachne.hello.greeter/greeting _]])]
    (if (not-empty existing-greeters)
      cfg
      (cfg/with-provenance :module `configure
        ;; the with-provenance macro adds provenance txdata (including the
        ;; current function and stack) to every config update in its body.
        (let [tid (cfg/tempid)
              txdata [{:db/id tid
                       :arachne.hello.greeter/greeting "Hello, world!"
                       :arachne.component/constructor :arachne.hello/construct-greeter}]
              txdata (concat txdata
                       (map (fn [rt]
                              [:db/add rt :arachne.runtime/components tid])
                         (all-runtimes cfg)))]
          (cfg/update cfg txdata))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; This section contains DSL functions to allow users to create greeters easily

;; Spec for the greeter DSL form. Re-uses the spec for an Arachne ID from
;; arachne.core.dsl.specs
(s/fdef greeter
  :args (s/cat :id ::dslspecs/id
               :message string?))

(dsl/defdsl greeter
  "Create a greeter component that will greet you with the specified message
  when the application starts. The component will be created with the specified
  Arachne ID. Returns the entity ID of the newly-created component."
  [id message]
  (let [tid (cfg/tempid)
        txdata [{:db/id tid
                 :arachne/id id
                 :arachne.hello.greeter/greeting message
                 :arachne.component/constructor :arachne.hello/construct-greeter}]]
    (cfg/resolve-tempid (dsl/transact txdata) tid)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; This section contains the runtime implementation of a Greeter component

(defrecord Greeter [greeting]
  c/Lifecycle
  (start [this] (println greeting) this)
  (stop [this] this))

(defn construct-greeter
  "The constructor function for a Greeter component. Constructor functions can take 0, 1 or 2 arguments:

  - 0 args: the constructor will be invoked with no args.
  - 1 args: the constructor will be an entity map obtained by calling (pull '[*]) on the component entity.
  - 2 args: the constructor will be passed the config and the entity ID of the component entity"
  [entity-map]
  (->Greeter (:arachne.hello.greeter/greeting entity-map)))
