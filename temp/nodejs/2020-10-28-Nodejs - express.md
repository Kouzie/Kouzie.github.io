---
title:  "Nodejs - express!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - Nodejs

tags:
  - nuxt

toc: true
toc_sticky: true

---


## express

웹서버 개발시 많이 사용되는 `express` 라는 패키지를 설치하여 서버를 실행해보자.  

```
npm install express --save
```

```js
const express = require('express');
const app = express();

app.get('/', (req, res, next) => {
    res.send('/ get 요청')
});

app.post('/', (req, res, next) => {
    res.send('/ post 요청')
});

app.get('/user', (req, rep) => {
    const data = { name: 'ko', age: 25 };
    rep.send(data);
});

app.get('/error', (req, res) => {
    res.status(404).send('404 Error');
});

app.listen(5000, () => {
    console.log('server is running, http://localhost:5000');
});
```

`http://localhost:5000/user` 으로 접속시 아래 문자열이 출력된다.  

```
{"name":"ko","age":25}
```

`express` 에선 객체 출력시 자동으로 `JSON` 형식으로 출력한다.  


`http` 모듈과 `if` 문을사용해 `ulr`, `method` 구분 코드를 덕지덕지 붙여 사용하는 것 보다  
`express` 제공함수를 사용하는 것이 간결하고 깔끔하다.  

### query, path param 처리

```js
app.get('/users/:id', (req, res, next) => {
    let params = req.params;
    let querys = req.query;
    console.log(params)
    console.log(querys);
    res.send('hello world');
});

app.listen(5000, () => {
    console.log('server is running, http://localhost:5000');
});
```

http://localhost:5000/users/2?a=10&b=20

콘솔 출력값
```
{ id: '2' }
{ a: '10', b: '20' }
```

### json request body 처리


```js
const bodyParser = require('body-parser');
app.use(bodyParser.json());

app.post('/', (req, res, next) => {
    let body = req.body;
    console.log(body); // { test: 1 }
    res.send("/ post 요청");
});

app.listen(5000, () => {
    console.log('server is running, http://localhost:5000');
});
```

rest client 툴로 `json` 타입으로 `body` 에  `{"test":1}` 설정해서 전송  


### 응답 데이터 설정

```js
app.get('/send', (req, res, next) => {
    res.send('<h1>hello world</h1>'); // 요소 태그가 있으면 자동으로 html 타입으로 반환
});

app.get('/download', (req, res, next) => {
    res.download('./test.txt') // 파일 다운로드
});

app.get('/redirect', (req, res, next) => {
    res.redirect('/send') // send 로 강제이동 
});

app.get('/json', (req, res, next) => {
    res.json({ message: 'success', code: 0 }) // body 에 json 형태로 데이터 응답
});

app.listen(5000, () => {
    console.log('server is running, http://localhost:5000');
});
```

### 라우터  

`app.메서드([url], 처리 람다식)` 형식으로 라우팅을 할 수 도 있겠지만 express 에서 제공하는 Router 를 사용하면 모듈 형식으로 코드구성이 가능하다.  


```js
// app.js
const express = require('express');
const users = require('./users.js')
const boards = require('./boards.js')
const app = express();

app.use('/users', users);
app.use('/boards', boards);

app.listen(5000, () => {
    console.log('server is running, http://localhost:5000');
});
```

`app.js` 에선 `app.use` 메서드를 사용해 정의한 라우터만 등록한다.  

```js
// users.js
const express = require('express');
let router = express.Router();

router.get('/:id', (req, res, next) => {
    res.send('user get')
})
router.post('/', (req, res, next) => {
    res.send('user post')
})
router.put('/:id', (req, res, next) => {
    res.send('user put')
})
router.delete('/:id', (req, res, next) => {
    res.send('user delete')
})
module.exports = router;
```

`boards.js` 도 `user` 부분을 `boards` 로만 출력하도록 수정하여 작성  

`GET`, `PUT`, `DELETE` 메서드를 사용해 `http://localhost:5000/users/1` url 로 접근하여 위에 설정한 데이터가 `body` 에 출력되는지 확인  



### 미들웨어

스프링의 필터같은 개념
위의 `app.js` 에 `use` 메서드를 사용해 미들웨어 등록  

```js
app.use((req, res, next) => {
    console.log('first middle ware')
    next();
})

app.use('/users', users);
app.use('/boards', boards);

app.listen(5000, () => {
    console.log('server is running, http://localhost:5000');
});
```

만약 어떠한 예외로 인해 `next()` 메서드를 호출하지 못할경우 클라이언트는 timeout 될때까지 대기하기 때문에 `res.send()` 같은 메서드로 반환해주어야 한다.  

사실 우리가 위에서 사용했던 request body 를 읽기 위한 `bodyParser` 도 미들웨어이다.  

```js
app.use(bodyParser.json());
```

또한 `app.get((req, res, next) => {}), route.get(...)` 메서드에서의 람다식 또한 범위가 좁혀진 일종의 미들웨어라 할 수 있다.  

```js
var getMiddleWare = (req, res, next) => {
    console.log('get middle ware invoked')
    next()
}

router.get('/:id', getMiddleWare, getMiddleWare, (req, res, next) => {
    res.send('user get')
})
```

앞에 url 이 있다면 해당 범위에서 동작하는 미들웨어  
url 없다면 모든 경우에서 동작하는 미들웨어이다.  

위처럼 `getMiddleWare` 2번, 람다식을 사용하면 `getMiddleWare` 가 2번 호출 후 람다식이 실행된다.  

`next` 가 다음 미들웨어에게 프로세스를 인계해준다 생각하면 된다.

![express1](/assets/nodejs/express1.png){: .shadow}     


#### 유용한 미들웨어들   

```js
const logger = require('morgan');
const bodyParser = require('body-parser');
const cookieParser = require('cookie-parser');
app.use(logger('dev'));
// dev 포맷의 경우 아래값을 반환, 이외에도 여러값이 있으니 확인 바람
// GET /users/1 200 0.966 ms - 8
app.use(cookieParser()); // 해더쿠키를 js 객체형식으로 변환
app.use(bodyParser.json({limit:5000000})); // 5mb 까지 허용, default 100kb
```



### express-generate

`express` 역시 `Spring MVC` 구조처럼 일련된 구조를 가지며 이를 자동으로 생성해주는 패키지가 있다.  

```
$ express test-express
$ cd test-express
$ tree ./
./
├── app.js
├── bin
│   └── www
├── package.json
├── public
│   ├── images
│   ├── javascripts
│   └── stylesheets
│       └── style.css
├── routes
│   ├── index.js
│   └── users.js
└── views
    ├── error.jade
    ├── index.jade
    └── layout.jade

7 directories, 9 files
```

`package.json` 에는 아래와 같은 패키지들이 자동 포함된다.  

```json
{
  "name": "test-express",
  "version": "0.0.0",
  "private": true,
  "scripts": {
    "start": "node ./bin/www"
  },
  "dependencies": {
    "cookie-parser": "~1.4.4",
    "debug": "~2.6.9",
    "express": "~4.16.1",
    "http-errors": "~1.6.3",
    "jade": "~1.11.0",
    "morgan": "~1.9.1"
  }
}
```

`app.js` 에서도 필수미들웨어, 에러미들웨어, 각종 라우팅 파일이 자동 생성되며 사용설정 된다.  

```js
var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');

var indexRouter = require('./routes/index');
var usersRouter = require('./routes/users');

var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/users', usersRouter);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
```

`express` 에서 defalut html 렌더링 엔진으로 `jade` 를 사용하는데 `ejs` 를 사용하고 싶다면 `--ejs` 옵션을 사용해 프로젝트를 생성.

```
$ express --ejs test-express
```

`npm start` 를 통해 실행되는 `node ./bin/www`  명령 대신 `supervisor`, `pm2`, `forever` 등의 실행 패키지를 사용해도 된다.  


## express with nuxt

`nuxt` 와 `express` 가 같이 설정된 template 설치  

```
$ vue init nuxt-community/starter-template nuxt-with-express
$ cd nuxt-with-express
```

`express` 서버 실행  

```
$ npm install
$ node api/index.js # express server 시작
```

nuxt 도 같이 실행시키고 싶다면 `nuxt` 명령 이 설정된 `npm` 명령 사용  

```
$ npm run build # es6를 실행가능한 코드로 해석, .nuxt 디렉토리 생성
$ npm run start 
```

## mysql 연동

```
$ npm install -s mysql
```


```js
const mysql = require('mysql');

let db = mysql.createConnection({
    host: '127.0.0.1',
    port: '3306',
    user: 'root',
    password: 'root',
    database: 'boards'
})

db.query(`SELECT * FROM users`, (err, data) => {
    if (err) console.log('err occured! ' + err);
    else 
        data.map(item => {
            console.log(`user id: ${item.id}, name: ${item.name}, age: ${item.age}`)
        })
});
```

동적쿼리 수행을 위해 문자열을 조합하거나 `?` 키워드로 파라미터를 전달할 수 있다.   

```js
db.query(`SELECT * FROM users WHERE id=${id}`, (err, data) => {...}

db.query(`SELECT * FROM users WHERE id=?`, [id], (err, data) => {...}
```

### ORM - sequelize

```
$ npm install -s mysql2
$ npm install -s sequelize
```

`sequelize` 내부에서 `mysql2` 모듈을 사용함으로 설치 필요  

```js
const Sequelize = require('sequelize')

const scheme = 'boards'
const user = 'root'
const password = 'root'
const options = {
    host: '127.0.0.1',
    dialect: 'mysql',
    logging: false
}
let sequelize = new Sequelize(scheme, user, password, options);
```

DB 커넥션을 위한 정보를 `sequelize` 생성시 전달  

```js
const DataTypes = Sequelize.DataTypes;

let users = sequelize.define('users', {
    id: {
        type: DataTypes.INTEGER(11),
        primaryKey: true, // default false
        autoIncrement: true, // default false
        allowNull: false // default true
    },
    name: {
        type: DataTypes.STRING(255),
        allowNull: false
    },
    age: {
        type: DataTypes.INTEGER(11),
        defaultValue: 25
    }
}, {
    tableName: 'users',
    freezeTableName: false, // tableName 속성으로 테이블명 변경
    timestamps: false // createdAt, updatedAt 생성 유무
});
```
`sequelize.define()` 메서드를 통해 생성된 users 객체로 테이블을 컨트롤할 수 있는 쿼리를 메서드 형식으로 사용할 수 있다.  

```js
users.findAll({ raw: true }).then((users) => {
    users.forEach(user => {
        console.log(`id:${user.id}, name:${user.name}, age:${user.age}`);
    });
});
```

```
id:1, name:hong, age:25
id:2, name:ko, age:26
id:3, name:jo, age:23
```

만약 DB 에 테이블이 없다면 아래처럼 `sequelize.sync()` 메서드로 정의해둔 테이블 생성이 가능.  

```js
sequelize.sync().then(() => {
    console.log('sync success');
    users.findAll({ raw: true }).then((users) => {
        users.forEach(user => {
            console.log(`id:${user.id}, name:${user.name}, age:${user.age}`);
        });
        console.log("user pint success");
    });
}).catch((err) => {
    console.log('sync failed, err:' + err);
});
```

생성 후에 users 테이블에서 데이터를 조회한다.  

#### sequelize model 관리

효율적인 테이블 관리, 디비 연결객체를 사용하기위해 모듈화하여 관리한다.  

```js
// users.js
module.exports = function (sequelize, DataTypes) {
    let users = sequelize.define('users', {
        ... // 위의 users 동일
    }
    // boards 테이블과 1:N 관계
    users.associate = function (models) {
        users.hasMany(models.boards);
    }
    return users;
}

// boards.js
module.exports = function (sequelize, DataTypes) {
    let boards = sequelize.define('boards', {
        id: {...}, 
        title: {...}, // varchar
        content: {...}, //text
        userId: {...},
    }, {
        tableName: 'boards',
        freezeTableName: false,
        timestamps: false
    });
    // users 테이블과 N:1 관계
    boards.associate = function (models) {
        boards.belongsTo(models.users);
    }
    return boards;
}
```

테이블 정의를 위한 js 파일 `users.js, boards.js` 생성  

```js
const Sequelize = require('sequelize')
const fs = require('fs');
const path = require('path')

const scheme = 'boards'
const user = 'root'
const password = 'root'
const options = {
    host: '127.0.0.1',
    dialect: 'mysql',
    logging: false
}

let sequelize = new Sequelize(scheme, user, password, options);
let db = {};

fs.readdirSync(__dirname)
    .filter(function (file) {
        return (file.indexOf('.') !== 0) && (file !== 'index.js');
    }).forEach(function (file) {
        let model = require(path.join(__dirname, file))(sequelize, Sequelize.DataTypes);
        console.log(model)
        db[model.name] = model;
    })
Object.keys(db).forEach(function (modelName) {
    if ("associate" in db[modelName]) {
        db[modelName].associate(db);
    }
})
db.sql = sequelize;
db.S = Sequelize;
sequelize.sync();
module.exports = db;
```

DB 연결을 위한 `sequelize`, 테이블 관리객체를 `db` 객체에 필드로 집어넣고 `exports` 하여 사용  

