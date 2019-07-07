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

(defn read-token-or-pass []
  (prn "Please enter your API token for basic auth: ")
  (clojure.string/trim (str (read))))

(defn maybe-ask [s]
  (if (= :ask s)
    (read-token-or-pass)
    s))

(defn get-auth-type-token [{:keys [username token-or-pass]}]
  (let [use-token (maybe-ask token-or-pass)]
    {:method :basic
     :username username
     :token-or-pass use-token}))

(defn get-auth-type-cookie [{:keys [cookie-file]}]
  (let [cookie (slurp cookie-file)]
    {:method :cookie
     :cookie cookie}))

(defn get-auth-from-rc [{:keys [method] :as m}]
  (case method
    :basic (get-auth-type-token m)
    :cookie (get-auth-type-cookie m)
    (throw "Unsupported :method type in :auth block of RC file!")))

(defn get-rc-file []
  (let [rc (get-rc-file-raw)
        provider-name (:provider rc)
        provider (provider-name rc)
        domain (:domain provider)
        auth (:auth provider)]
    {:auth (get-auth-from-rc auth)
     :domain domain
     :provider provider-name}))

(defn set-conf! []
  (reset! *conf (get-rc-file)))

(defn get-domain [] (:domain @*conf))
(defn get-provider [] (:provider @*conf))
(defn get-auth [] (:auth @*conf))
