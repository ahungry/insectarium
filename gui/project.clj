(defproject gui "0.1.0-SNAPSHOT"
  :description "insectarium - a gui for bug tracking"
  :url "https://github.com/ahungry/insectarium"
  :license {:name "GPL-3.0-or-later WITH Classpath-exception-2.0"
            :url "http://www.gnu.org/licenses/"}
  :plugins [[io.aviso/pretty "0.1.37"]]
  :middleware [io.aviso.lein-pretty/inject]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cljfx "1.2.9"]
                 [clj-http "3.10.0"]
                 [cheshire "5.8.1"]
                 [slingshot "0.12.2"]
                 ;; readability things
                 [io.aviso/pretty "0.1.37"]
                 [expound "0.7.2"]
                 ;; end rt
                 [org.clojure/java.jdbc "0.7.9"]
                 [org.xerial/sqlite-jdbc "3.27.2.1"]]
  :main ^:skip-aot gui.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :injections [(javafx.application.Platform/exit)]}})
