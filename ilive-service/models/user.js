'use script';

const pool = require('./db.js');

function handleSqlErr(pool, reject, type) {
    if (pool && typeof pool.getConnection != 'function') {
        reject({
            ret: 1,
            type,
        });
        return true;
    }
    return false;
}
module.exports = {
    checkUser(code) {
        return new Promise((resolve, reject) => {
            if (pool && typeof pool.getConnection != 'function') {
                reject({
                    ret: 1,
                    type: 'err'
                });
                return;
            }
            pool.getConnection((err, connection) => {
                if (err) {
                    reject({
                        ret: 1,
                        type: 'connect err',
                    });
                    return;
                }
                const sql = 'select * from user where nickname = ' + connection.escape(code);
                connection.query(sql, (err, results, fields) => {
                    connection.release();
                    if (err) {
                        reject({ ret: 2, type: 'select error' });
                        return;
                    }
                    resolve({
                        ret: 0,
                        type: 'success',
                        results
                    });
                });
            })
        })
    },

    registered(nickname, password) {
        return new Promise((resolve, reject) => {
            if (pool && typeof pool.getConnection != 'function') {
                reject({
                    ret: 1,
                    type: "error"
                });
                return;
            }
            pool.getConnection((err, connection) => {
                if (err) {
                    reject({
                        ret: 1,
                        type: 'get conntion error',
                    });
                    return;
                }
                const sql = 'insert into user set ?';
                connection.query(sql, { nickname, password }, (err, results, fields) => {
                    connection.release();
                    if (err) {
                        reject({
                            ret: 2,
                            type: 'insert error'
                        });
                        return;
                    }
                    resolve({
                        ret: 0,
                        type: 'success',
                        results
                    });
                })
            });
        });
    },
    generatorCode(uid, code) {
        return new Promise((resolve, reject) => {
            if (pool && typeof pool.getConnection != 'function') {
                reject({
                    ret: 1,
                    type: "error"
                });
                return;
            }
            pool.getConnection((err, connection) => {
                if (err) {
                    reject({
                        ret: 1,
                        type: 'get conntion error',
                    });
                    return;
                }
                const sql = 'insert into livecode set ?';
                connection.query(sql, { uid, code }, (err, results, fields) => {
                    connection.release();
                    if (err) {
                        reject({
                            ret: 2,
                            type: 'insert error'
                        });
                        return;
                    }
                    resolve({
                        ret: 0,
                        type: 'success',
                        results
                    });
                })
            });
        });
    }
}