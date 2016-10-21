(ns arachne.hello-test
  (:require [clojure.test :refer :all]
            [arachne.core :as core]
            [com.stuartsierra.component :as c]
            [arachne.core.config :as cfg]
            [clojure.string :as str]))

(deftest greeters-test
  (testing "If we give a config with no greeter, the default 'hello world' one is used."
    (let [cfg (core/build-config  '[:org.arachne-framework/hello-module]
                "test-configs/greeters.clj")
          rt (core/runtime cfg :test/runtime)
          output (with-out-str (c/stop (c/start rt)))]
      (is (re-find #"Hola!" output))
      (is (re-find #"Hi!" output)))))

(deftest missing-greeters-test
  (testing "If we give a config with no greeter, the default 'hello world' one is used."
    (let [cfg (core/build-config  '[:org.arachne-framework/hello-module]
                "test-configs/missing-greeters.clj")
          rt (core/runtime cfg :test/runtime)
          output (with-out-str (c/stop (c/start rt)))]
      (is (re-find #"Hello, world!" output)))))