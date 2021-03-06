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

(defn read-token-or-pass [username provider-name]
  (prn
   (format "Please enter your API token for user: %s under provider %s: "
           username
           provider-name))
  (clojure.string/trim (str (read))))

(defn maybe-ask [s username provider-name]
  (if (= :ask s)
    (read-token-or-pass username provider-name)
    s))

(defn get-auth-type-token [{:keys [username token-or-pass provider-name]}]
  (let [use-token (maybe-ask token-or-pass username provider-name)]
    {:method :basic
     :username username
     :token-or-pass use-token}))

(defn get-auth-type-cookie [{:keys [cookie-file]}]
  (let [cookie (slurp cookie-file)]
    {:method :cookie
     :cookie cookie}))

(defn maybe-throw-for-method [provider]
  (prn provider)
  (when (not (= :stub provider))
    (throw (Throwable. "Unsupported :method type in :auth block of RC file!"))))

(defn get-auth-from-rc [{:keys [method] :as m} provider]
  (case method
    :basic (get-auth-type-token (conj m {:provider-name provider}))
    :cookie (get-auth-type-cookie m)
    (maybe-throw-for-method provider)))

(defn make-provider
  "Do the setup work to create a provider (ask for user passwords etc.)."
  [rc provider-name]
  (let [provider (provider-name rc)
        domain (:domain provider)
        auth (:auth provider)]
    {:auth (get-auth-from-rc auth provider-name)
     :domain domain
     :provider provider-name}))

(defn get-rc-file []
  (let [rc (get-rc-file-raw)
        providers (:providers rc)]
    (zipmap providers (map (partial make-provider rc) providers))))

(defn set-conf! []
  (reset! *conf (get-rc-file)))

(defn get-domain [k] (:domain (k @*conf)))
(defn get-provider [k] (:provider (k @*conf)))
(defn get-auth [k] (:auth (k @*conf)))
(defn get-providers [] (keys @*conf))
