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
    updateNum() {

    }
}