'use script';

const pool = require('./db.js');

module.exports = {
    startLive(code) {
        return new Promise((resolve, reject) => {
            if (!pool && !pool.getConnection) {
                reject({
                    ret: 1,
                    result: 'connect to server err',
                });
                return;
            }
            pool.getConnection((err, connection) => {
                if (err) {
                    reject({
                        ret: 1,
                        result: 'get connection err',
                    });
                    console.log(JSON.stringify(err));
                }
                const sql = 'insert into liveroom (code_id, state) ' +
                    'select id, 1 from livecode where code = ' +
                    (connection.escape(code)) +
                    ' and not exists (' +
                    'select * from liveroom where state = 1 and code = ' +
                    (connection.escape(code)) +
                    ')';
                console.log(sql);
                connection.query(sql, (err, results, fields) => {
                    if (err) {
                        reject({
                            ret: 1,
                            result: 'query and insert err',
                            error: err
                        });
                        return;
                    }
                    resolve({
                        ret: 0,
                        result: results,
                    });
                })
            })
        })
    },
    updateNum(type, code) {
        return new Promise((resolve, reject) => {
            pool.getConnection((err, connection) => {
                if (err) {
                    reject({ ret: 1, results: 'get connection error' });
                    return;
                }

                let sql;
                if (type == 0) { //add
                    sql = 'update liveroom ' +
                        'left join livecode ' +
                        'on liveroom.code_id = livecode.id ' +
                        'set audience = audience + 1 ' +
                        'where state = 1';
                } else {
                    sql = 'update liveroom ' +
                        'left join livecode ' +
                        'on liveroom.code_id = livecode.id ' +
                        'set audience = audience - 1 ' +
                        'where state = 1 ' +
                        'and audience > 0 ';
                }
                connection.query(sql, (err, results, fields) => {
                    if (err) {
                        reject({ ret: 1, results: 'update audience error' });
                        return;
                    }
                    resolve({
                        ret: 0,
                        results,
                    })
                })
            })
        });
    },
    liveEnd(code) {
        return new Promise((resolve, reject) => {
            pool.getConnection((err, connection) => {
                if (err) {
                    reject({ ret: 1, results: err });
                    return;
                }
                const sql = 'update liveroom ' +
                    'left join livecode ' +
                    'on liveroom.code_id = livecode.id ' +
                    'set state = 0, end_date = ' + "'" +
                    getMysqlTimestampFormat(new Date()) +
                    "' where state = 1";
                console.log(sql);
                connection.query(sql, (err, results, fields) => {
                    if (err) {
                        reject({ ret: 1, results: err });
                        return;
                    }
                    resolve({ ret: 0, results });
                });
            })
        })
    },
    getHotLive(index, number) {
        return new Promise((resolve, reject) => {
            pool.getConnection((err, connection) => {
                if (err) {
                    reject({ ret: 1, results: err });
                    return;
                }
                const sql = 'select nickname, code, audience, total, ' +
                    "'http:\/\/192.168.2.1/liveavatar.jpeg' as liveAvatar " +
                    'from liveroom, livecode, user, ' +
                    '(select count(code_id) as total from liveroom where state = 1) as page ' +
                    'where state = 1 ' +
                    'and liveroom.code_id = livecode.id ' +
                    'and livecode.uid = user.uid ' +
                    'order by audience desc ' +
                    'limit ' + index + ', ' + number;
                console.log(sql);
                connection.query(sql, (err, results, fields) => {
                    if (err) {
                        reject({ ret: 1, results: err });
                        return;
                    }
                    resolve({ ret: 0, results });
                })
            });
        });
    }
};

function twoDigits(d) {
    if (0 <= d && d < 10) return "0" + d.toString();
    if (-10 < d && d < 0) return "-0" + (-1 * d).toString();
    return d.toString();
}

function getMysqlTimestampFormat(date) {
    return date.getFullYear() + "-" + twoDigits(1 + date.getMonth()) + "-" + twoDigits(date.getDate()) + " " + twoDigits(date.getHours()) + ":" + twoDigits(date.getMinutes()) + ":" + twoDigits(date.getSeconds());
}