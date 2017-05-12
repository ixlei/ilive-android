'use script';
const express = require('express');
const Router = express.Router();

const userDb = require('../models/user.js');

Router.post("/login", (req, res, next) => {
    const { nickname, password } = req.body;
    console.log(JSON.stringify(req.session));
    userDb.checkUser(nickname)
        .then((data) => {
            if (data && data.ret == 0) {
                const results = data.results;
                if (results.length) {
                    const item = results[0];
                    if (item.password == password) {
                        res.json({
                            ret: 0,
                            result: 'success',
                            code: item.code
                        });
                        req.session.user = nickname;
                    } else {
                        res.json({
                            ret: 3,
                            result: 'password not match user nickname',
                        })
                    }
                } else {
                    res.json({
                        ret: 2,
                        result: 'select error'
                    });
                }
            }
        }, (err) => {
            res.json({
                ret: 1,
                result: 'database error',
            });
        });
});

Router.post("/register", (req, res, next) => {
    const { nickname, password } = req.body;

    userDb.registered(nickname, password)
        .then((result) => {
            console.log(JSON.stringify(result));
            if (result && result.results) {
                let item = result.results;
                let code = parseInt(Math.random() * (1 << 24), 10);
                console.log('insert code', code);
                req.session.user = nickname;
                userDb.generatorCode(item.insertId, code)
                    .then((data) => {
                        res.json({
                            ret: 0,
                            result: code
                        });
                    }, (err) => {
                        res.json({
                            ret: 1,
                            result: 'error'
                        });
                        console.log(JSON.stringify(err));
                    });
            } else {
                res.json({
                    ret: 1,
                    type: 'error',
                })
            }
        }, (err) => {
            const code = err.error.code;
            if (code == 'ER_DUP_ENTRY') {
                res.json({
                    ret: 3,
                    type: 'duplicate',
                    result: '已经存在'
                });
                return;
            }
            res.json(JSON.stringify(err));
            console.log(JSON.stringify(err));
        });
});

function runTask(cb) {
    return new Promise((resolve, reject) => {
        const task = cb();
        let res = task.next();

        function step() {
            if (!res.done) {
                res.value.then((data) => {
                    res = task.next(data);
                    step();
                }, (err) => {
                    reject(err);
                });
            } else {
                resolve(res.value);
            }
        }

        step();
    });
}

module.exports = Router;