(ns examples.websockets-demo)

(def state {:USER/BY-ID                      {},
            :fulcro.inspect.core/app-id      :some-app-id,
            :LOGIN-FORM-UI                   {:UI {:db/id :UI, :ui/username "current-app"}},
            :CHAT-ROOM/BY-ID
                                             {1
                                              {:db/id                                    1,
                                               :websocket-demo.schema/chat-room-messages [],
                                               :websocket-demo.schema/chat-room-title    "General Discussion",
                                               :active-user-panel                        [:UI-ACTIVE-USERS :UI]}},
            :ui/locale                       :en,
            :root/all-users                  [],
            :fulcro/loads-in-progress        #{},
            :websocket-demo.schema/chat-room [:CHAT-ROOM/BY-ID 1],
            :fulcro/ready-to-load            (),
            :ui/loading-data                 false,
            :UI-ACTIVE-USERS                 {:UI {}},
            :root/login-form                 [:LOGIN-FORM-UI :UI]})

(def config {:table-name :UI-ACTIVE-USERS})
