(ns examples.so-question)

(def state {:app/session [:session/by-id 1],
            :app/messages [[:message/by-id 100] [:message/by-id 101]],
            :app/users [[:user/by-id 200] [:user/by-id 201]],
            :message/by-id
            {100 {:id 100, :text "Message 1"}, 101 {:id 101, :text "Message 2"}},
            :user/by-id
            {200 {:id 200, :email "1@foo.com"},
             201 {:id 201, :email "2@foo.com"}},
            :session/by-id {1 {:id 1, :app/messages [[:message/by-id 100]]}}})
