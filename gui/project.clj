(defproject gui "0.1.0-SNAPSHOT"
  :description "insectarium - a gui for bug tracking"
  :url "https://github.com/ahungry/insectarium"
  :license {:name "GPL-3.0-or-later WITH Classpath-exception-2.0"
            :url "http://www.gnu.org/licenses/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [cljfx "1.2.9"]
                 [clj-http "3.10.0"]
                 [cheshire "5.8.1"]
                 [slingshot "0.12.2"]
                 [org.clojure/java.jdbc "0.7.9"]
                 [org.xerial/sqlite-jdbc "3.27.2.1"]]
  :main ^:skip-aot gui.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :injections [(javafx.application.Platform/exit)]}})
