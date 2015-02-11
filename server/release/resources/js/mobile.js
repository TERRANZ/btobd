var loaded = 0;
var feedId = 0;

$(document).on("pageshow", load_main())

function load_main() {
    var url = document.URL;
    load_info();
}


function load_info() {
    $.ajax({
        url: '/btobd/obd/current.get.json',
        async: false,
        type: 'get',
        data: {},
        success: function(data) {
            var htmlRet = "";
            if (data.errorCode == 0) {
                htmlRet += create_main_post(data.userId, data.command, data.result, data.id);
                $("#currstate").html(htmlRet);
            }
        }
    });

    $.ajax({
        url: '/btobd/obd/do.list.json',
        async: false,
        type: 'get',
        data: {},
        success: function(data) {
            var htmlRet = "<table border='1'>";
            htmlRet += "<tr>";
            htmlRet += "<th>Время</th>";
            htmlRet += "<th>Параметр</th>";
            htmlRet += "<th>Значение</th>";
            htmlRet += "</tr>";
            if (data.errorCode == 0) {
                  $.each(data.data, function(i, data) {
                    htmlRet += create_main_post(data.id, data.command, data.result, data.timestamp, data.id);
                  });
                  htmlRet += "</table>";
                  $("#messages").html(htmlRet);
            }
        }
    });
}

//function open_feed(fid) {
//    window.location.href = "/jbrss/ui/main#feed=" + fid;
//    load_feed(fid);
//}
//
//function load_feed(fid) {
//    loaded = 10;
//    feedId = fid;
//    load();
//    $("#feeds_collapsable").trigger('collapse');
//    $("#messages_collapsable").trigger('expand');
//}

function create_main_post(title, command, result, date, id) {
    var ret = "<tr>";
    var d = new Date(date);
    ret += "<td>"+d+"</td>";
    ret += "<td>"+command+"</td>";
    ret += "<td>"+result+"</td>";
    ret += "</tr>";
    return ret;
}

function userLogin() {
    if (!$("#loginbtn").hasClass('ui-disabled'))
        $("#loginbtn").addClass('ui-disabled');
    var user = $("#j_username").val();
    var pass = $("#j_password").val();
    if (user == null || user.length == 0) {
        alert('Не указан логин');
    } else if (pass == null || pass.length == 0) {
        alert('Не введен пароль');
    } else {
        $.ajax({
            url: '/btobd/login/do.login.json',
            async: false,
            type: 'get',
            data: {
                user: user,
                pass: pass
            },
            success: function(data) {
                $("#loginbtn").removeClass('ui-disabled');
                if (data.logged == true) {
                    setCookie("JSESSIONID", data.session);
                    setCookie("username", user);
                    window.location.assign("/btobd/ui/main");
                } else {
                    if (parseInt(data.errorCode) == 1000) {
                        alert('Вы сделали слишком много попыток зайти на ресурс. Аккаунт временно заблокирован. Попробуйте еще раз через некоторое время.');
                    } else if (parseInt(data.errorCode) == 1001) {
                        alert('Ваш аккаунт заблокирован, обратитесь в поддержку для получения полной информации.');
                    } else {
                        alert('Неверный логин или пароль');
                        $('#j_password').val('');
                    }
                }
            }
        });
    }
}

function setCookie(key, value) {
    var expires = new Date();
    expires.setTime(expires.getTime() + (1 * 24 * 60 * 60 * 1000));
    document.cookie = key + '=' + value + ';path=/' + ';expires=' + expires.toUTCString();
}

function learnRegExp(s) {
    var regexp = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
    return regexp.test(s);
}

function userReg() {
    var user = $("#user").val();
    var pass = $("#pass").val();
    var capval = $("#captcha_val").val();
    if (user == null || user.length == 0) {
        alert('Не указан логин');
    } else if (pass == null || pass.length == 0) {
        alert('Не введен пароль');
    } else {
        $.ajax({
            url: '/btobd/login/do.register.json',
            async: true,
            type: 'get',
            data: {
                user: user,
                pass: pass
            },
            success: function(data) {
                if (data.logged == true) {
                    setCookie("JSESSIONID", data.session);
                    window.location.assign("/btobd/ui/main");
                } else {
                    if (parseInt(data.errorCode) == 1000) {
                        alert('Вы сделали слишком много попыток зайти на ресурс. Аккаунт временно заблокирован. Попробуйте еще раз через некоторое время.');
                    } else if (parseInt(data.errorCode) == 1001) {
                        alert('Ваш аккаунт заблокирован, обратитесь в поддержку для получения полной информации.');
                    } else {
                        alert(data.message);
                        $('#j_password').val('');
                    }
                }
            }
        });
    }
}

function saveSettings() {
    $.ajax({
        url: '/jbrss/settings/do.set.json',
        async: true,
        type: 'get',
        data: {
            key: $("#key").val(),
            val: $("#val").val(),
        },
        success: function(data) {
            window.location.assign("/btobd/ui/main");
        }
    });
}

function settings() {
    window.location.assign("/btobd/ui/setting");
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1);
        if (c.indexOf(name) != -1) return c.substring(name.length,c.length);
    }
    return "";
}