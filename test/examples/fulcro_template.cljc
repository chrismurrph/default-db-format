(ns examples.fulcro-template
  (:require [clojure.test :refer :all]))

(def initial-state
  {:ui/react-key               "en-US",
   :fulcro.ui.bootstrap3.modal/by-id
                               {:welcome
                                {:db/id          :welcome,
                                 :modal/active   false,
                                 :modal/visible  false,
                                 :modal/keyboard true,
                                 :modal/size     nil,
                                 :modal/backdrop true}},
   :ui/ready?                  true,
   :current-user               nil,
   :fulcro.inspect.core/app-id "fulcro-template.ui.root/Root",
   :fulcro.client.routing/routing-tree
                               {:login
                                      [{:target-router :page-router, :target-screen [:login :page]}],
                                :new-user
                                      [{:target-router :page-router, :target-screen [:new-user :page]}],
                                :preferences
                                      [{:target-router :page-router, :target-screen [:preferences :page]}],
                                :main [{:target-router :page-router, :target-screen [:main :page]}]},
   :root/modals
                               {:welcome-modal [:fulcro.ui.bootstrap3.modal/by-id :welcome]},
   :ui/locale                  "en-US",
   :loaded-uri                 "/login.html",
   :preferences                {:page {:id :preferences}},
   :login
                               {:page
                                {:id             :login,
                                 :ui/username    "joe@nowhere.com",
                                 :ui/password    "",
                                 :ui/server-down false,
                                 :ui/error       nil}},
   :logged-in?                 false,
   :pages                      [:fulcro.client.routing.routers/by-id :page-router],
   :fulcro.client.routing.routers/by-id
                               {:page-router
                                {:fulcro.client.routing/id            :page-router,
                                 :fulcro.client.routing/current-route [:login :page]}},
   :user/by-id
                               {"780c0f16-8b4a-46e8-a21c-c525db7431e3"
                                {:uid       "780c0f16-8b4a-46e8-a21c-c525db7431e3",
                                 :name      "",
                                 :password  "",
                                 :password2 ""},
                                "4bc9274d-c78d-422b-822a-1ee15fc947bf"
                                {:uid       "4bc9274d-c78d-422b-822a-1ee15fc947bf",
                                 :name      "",
                                 :password  "",
                                 :password2 ""}},
   :main                       {:page {:id :main}},
   :new-user
                               {:page
                                {:id :new-user,
                                 :form
                                     [:user/by-id
                                      "4bc9274d-c78d-422b-822a-1ee15fc947bf"]}}})
