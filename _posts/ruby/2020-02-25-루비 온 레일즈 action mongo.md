<!-- ---
title:  "루비 온 레일즈, Action, Mongoid!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - ruby

tags:
  - ruby on rails

toc: true
toc_sticky: true

---

## Action Cable

> https://edgeguides.rubyonrails.org/action_cable_overview.html

루비 온 레일즈에서 `WebSocket` 을 사용하기 위해 사용하는 클래스  

아래의 `Connection` 객체를 통해 클라이언트와 어떻게 연결되는지 알아보자.  

```rb
# app/channels/application_cable/connection.rb
module ApplicationCable
  class Connection < ActionCable::Connection::Base
    identified_by :current_user

    def connect
      self.current_user = find_verified_user
    end

    private
      def find_verified_user
        if verified_user = User.find_by(id: cookies.encrypted[:user_id])
          verified_user
        else
          reject_unauthorized_connection
        end
      end
  end
end
```

identified_by 는 나중에 특정 연결을 찾기위한 연결 식별자  

## ActionController

모든 컨트롤러가 상속받는 레일즈의 상위 클래스이다.  

자동 생성된 컨트롤러 클래스를 보면 아래처럼 되어있다.  

```rb
class BoardsController < ApplicationController
  ... 
  # CRUD
  ...
end
```
`ApplicationController`는 또 아래처럼 되어있다.  

```rb
class ApplicationController < ActionController::Base
end
```

```rb
class ApplicationController < ActionController::API
end
```

### ActionController::Base

Rails 컨트롤러가 제공하는 

### ActionController::API

`ActionController::Base` 에서 제공하는 모든 기능을 사요하지 않고 API 기준에서 필요한 기능만 있는 경량화된 컨트롤러 생성을 위한 클래스  





### rescue_from


## 라우팅 

> https://guides.rubyonrails.org/routing.html

`rails` 에선 클래스가 자동생성되고 `resources :...` 형식의 키워드를 통해 `GET, POST, UPDATE, DELETE` 방식의 request 매핑이 모두 자동으로 이루어 진다.  

커스텀 Request url 매핑을 진행하고 싶다면 위의 url 참고  

## Mongoid

다음은 몽고DB 사용시에 `rails generate` 명령을 통해 자동생성된 `model` 객체이다.  

```rb
class Post
  include Mongoid::Document
  field :title, type: String
  field :body, type: String
  has_many :comments, dependent: :destroy
end
```

> https://github.com/mongodb/mongoid/blob/master/lib/mongoid/document.rb

```rb
module Mongoid

  # This is the base module for all domain objects that need to be persisted to
  # the database as documents.
  module Document
    extend ActiveSupport::Concern
    include Composable
    include Mongoid::Touchable::InstanceMethods

    attr_accessor :__selected_fields
    attr_reader :new_record
    ...
    ...
```

`include Composable` 를 확인하자. `Composable` 모듈은 엄청난 양의 Mongo를 위한 모듈들을 include 하고있다.    
> https://github.com/mongodb/mongoid/blob/bdcf60bdb7ffaab2fdaa2f39b92e70658a851f1f/lib/mongoid/composable.rb#L26  

레일즈에서 몽고 DB 시 자주 사용되는 모듈의 메서드를 간단히 알아보자.  

### field  

> https://www.rubydoc.info/github/mongoid/mongoid/Mongoid/Fields/ClassMethods#field-instance_method

```rb
class Post
  include Mongoid::Document
  field :title, type: String
  field :body, type: String
  has_many :comments, dependent: :destroy
end
```

`Mongoid::Document` 객체 안의 field 클래스 메서드를 호출하고 매개변수로 속성명(심볼객체), `Map`객체를 파라미터로 전달한다.  

**Options Hash (options):**  
* :type (Class) — The type of the field.  
* :label (String) — The label for the field.  
* :default (Object, Proc) — The field's default  

### belongs_to, has_and_belongs_to_many, has_many, has_one

> https://www.rubydoc.info/github/mongoid/mongoid/Mongoid/Relations/Macros/ClassMethods

레일즈에서 db 연관관계를 설정하기 위한 메서드이다.  

RDB에서도 사용되는 개념이다.(물론 db구조가 다르기에 사용법도 약간 다름)   
> 참고: https://rubykr.github.io/rails_guides/association_basics.html  

모든 객체(테이블)에는 상관관계가 있다.  

`1:N` 관계에선 주객체(`고객 테이블`)와 주객체의 id를 외래키로 같는 보조객체(`주문 테이블`)가 있다.  
`1:1` 관계에선 보조객체(`고객 상세정보 테이블`)와 보조객체의 id를 외래키로 같은 주객체(`고객`)가 있다.  

rails에서 어떻게 매핑하는지 알아보자.  


|`method`|`desc`|  
|---|---|---|  
|`belongs_to`|보조객체에서 주객체와 N:1 연관관계 맺을때 사용, `주문-고객` 관계|  
|`has_one`|주객체에서 보조객체와 1:1 관계를 맺을때 사용, `고객-고객상세정보` 관계|  
|`has_many`|주객체에서 보조객체와 1:N 관계를 맺을때 사용, `고객-주문`관계|  
|`has_and_belongs_to_many`|두 객체가 서로 N:M 관계를 맺을때 사용, 연관관계를 위한 테이블이 추가생성된다, `상품-주문`관계|

> `belongs_to`, `has_one`, `has_many`는 모두 단방향 연관관계로 설정하는 가정 하에 설명이다.


### has_many :through, has_one :through

`N:N` 관게를 맺을때 사용

```rb
class Physician < ActiveRecord::Base
  has_many :appointments
  has_many :patients, :through => :appointments
end
 
class Appointment < ActiveRecord::Base
  belongs_to :physician
  belongs_to :patient
end
 
class Patient < ActiveRecord::Base
  has_many :appointments
  has_many :physicians, :through => :appointments
end

physician.patients = patients
patient.physicians = physicians
```

![rails1](/assets/ruby/rails1.png){: .shadow}  

`N:1` 관계를 맺을때 사용  

```rb
class Supplier < ActiveRecord::Base
  has_one :account
  has_one :account_history, :through => :account
end
 
class Account < ActiveRecord::Base
  belongs_to :supplier
  has_one :account_history
end
 
class AccountHistory < ActiveRecord::Base
  belongs_to :account
end
```

![rails2](/assets/ruby/rails2.png){: .shadow}  


### embedded_in, embeds_many, embeds_one

`embedded` 몽고db와 같은 `documentation` 시스템에서만 사용되는 관계이다.  

```rb
class Person
  include Mongoid::Document
  embeds_many :addresses
end

class Address
  include Mongoid::Document
  embedded_in :person
end
```

우선 `embedded_in`, `embeds_many` 관계를 보면 RDB의 `belongs_to`, `has_many` 와 비슷하다.  

하지만 `documentation`에선 주객체가 보조객체를 리스트형식으로 포함하고 있을 수 있다.  
즉 보조객체가 주객체에 포함될 경우에 `embedded` 키워드를 사용한다.  

#### relation 옵션 - `(name, options = {}, &block)`

`has_many`, `embedded_in` 등의 메서드에 매개변수는 최대 3개가 들어간다.  
관계를 맺을 테이블명을 필수로 들어가고  

`option`은 아래 링크를 참조    
> https://guides.rubyonrails.org/association_basics.html#options-for-belongs-to  
> https://guides.rubyonrails.org/association_basics.html#options-for-has-one  
> https://guides.rubyonrails.org/association_basics.html#options-for-has-many  
> https://guides.rubyonrails.org/association_basics.html#options-for-has-and-belongs-to-many  

`Proc(&block)`는 연관관계를 불러올때 조건문 걸고싶을 때 사용된다.  
> https://guides.rubyonrails.org/association_basics.html#scopes-for-belongs-to  
> https://guides.rubyonrails.org/association_basics.html#scopes-for-has-one  
> https://guides.rubyonrails.org/association_basics.html#scopes-for-has-many  
> https://guides.rubyonrails.org/association_basics.html#scopes-for-has-and-belongs-to-many  

`Mongoid relation` 옵션은 아래 사이트를 참고,  
> 참고 https://docs.mongodb.com/mongoid/current/tutorials/mongoid-relations/

### Mongoid::Timestamps  

> This module handles the behaviour for setting up document created at and updated at timestamps.

`Created, Short, Timeless, Updated` 모듈을 포함하고 있고 생성, 각 모듈에서 업데이트 시간을 필드로 가진다.  

```rb
class Dictionary < ApplicationModel
  include Mongoid::Document
  include Mongoid::Timestamps

  field :key, type: String
  field :value, type: Hash, default: {}
end
```

필드에는 `key`, `value` 값만 있지만 생성된 문서에는 `created_at`, `updated_at` 속성도 포함된다.  
 -->
