(defproject listio "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.2.2"]
                 [compojure "1.1.8"]]
  :ring {:handler listio.server/app}
  :target-path "target/%s")
