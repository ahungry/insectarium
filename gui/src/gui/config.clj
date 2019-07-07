;; Handle the user configuration settings
(ns gui.config)

(def *conf (atom {}))

(defn get-xdg-config-home []
  (or (System/getenv "XDG_CONFIG_HOME")
      (System/getProperty "user.home")))

(defn get-rc-file-raw []
  (let [defaults (read-string (slurp "../conf/default-rc"))
        home-rc (format "%s/.insectariumrc" (System/getProperty "user.home"))
        xdg-rc (format "%s/insectarium/insectariumrc" (get-xdg-config-home))]
    (conj
      defaults
      (if (.exists (clojure.java.io/file home-rc))
        (read-string (slurp home-rc)))
      (if (.exists (clojure.java.io/file xdg-rc))
        (read-string (slurp xdg-rc))))))

(defn read-token []
  (prn "Please enter your API token for basic auth: ")
  (clojure.string/trim (str (read))))

(defn maybe-ask [token]
  (if (= :ask token)
    (read-token)
    token))

(defn get-auth [{:keys [type method username token]}]
  (let [use-token (maybe-ask token)]
    {:type type
     :method method
     :username username
     :token use-token}))

(defn get-rc-file []
  (let [rc (get-rc-file-raw)
        provider-name (:provider rc)
        provider (provider-name rc)
        domain (:domain provider)
        method (:method provider)
        auth (:auth provider)]
    {:auth (get-auth auth)
     :domain domain
     :method method
     :provider provider-name}))

(defn set-conf! []
  (reset! *conf (get-rc-file)))
