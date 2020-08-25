// let a = {name:hello, age:10} -> qs.Stringfy(a) -> name=hello&age=13
import Qs from 'qs';

// 包装了ajax，方便http调用
import axios from 'axios'

import router from "../router";


// 通用公共方法，含回调
export const reqRealEndAsync = (method, baseUrl, url, params, callback) => {
    params.token = sessionStorage.getItem('token');
    return axios({
        timeout: 5000,
        baseUrl: baseUrl,
        method: method,
        url: url,
        headers: {
            'Content-type': 'application/x-www-form-urlencoded',
        },
        data: Qs.stringify(params),
        traditional: true,  // 若data为数组，设为false就需要split，设为true就直接为List<String>类型
    }).then(res => {
        let result = res.data;
        if (result.code == 1) {
            router.replace({
                path: "login",
                query: {
                    msg: result.message
                }
            });
        } else if (result.code == 0) {
            if (callback != undefined) {
                callback(result.code, result.message, result.data);
            }
        } else if (result.code == 2) {
            if (callback != undefined) {
                callback(result.code, result.message, result.data);
            }
        }
    });
};

// 通用公共方法，无回调
export const reqRealEnd = (method, baseUrl, url, params) => {
    params.token = sessionStorage.getItem('token');
    return axios({
        timeout: 5000,
        baseUrl: baseUrl,
        method: method,
        url: url,
        headers: {
            'Content-type': 'application/x-www-form-urlencoded',
        },
        data: Qs.stringify(params),
        traditional: true,  // 若data为数组，设为false就需要split，设为true就直接为List<String>类型
    });
};
