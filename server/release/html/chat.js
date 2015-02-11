    require(['converse'], function (converse) {
    var user = getCookie("user");
    var rid = new Date().getTime();
    var request = "<body content='text/xml; charset=utf-8'";
      request += "from='"+user+"@xn--80aafhfrpg0adapheyc1nya.xn--p1ai'";
      request += "hold='1'";
      request += "rid='"+rid+"'";
      request += "to='xn--80aafhfrpg0adapheyc1nya.xn--p1ai'";
      request += "route='xn--80aafhfrpg0adapheyc1nya.xn--p1ai:5222'";
      request += "ver='1.6'";
      request += "wait='60'";
      request += "ack='1'";
      request += "xml:lang='en'";
      request += "xmlns='http://jabber.org/protocol/httpbind'/>";

      $.ajax({
        type: "POST",
        dataType: "xml",
        data: request,
        url: "http://xn--80aafhfrpg0adapheyc1nya.xn--p1ai:8182/bosh/httpbind",
        success: function( data ) {
            var sid = $(data).find("sid").text();
            var jid = user+"@xn--80aafhfrpg0adapheyc1nya.xn--p1ai";
            converse.initialize({
            bosh_service_url: 'http://xn--80aafhfrpg0adapheyc1nya.xn--p1ai:8182/bosh/',
            i18n: locales['en'],
            keepalive: true,
            message_carbons: true,
            play_sounds: true,
            roster_groups: true,
            sid : sid,
            rid : rid,
            jid : jid,
            show_controlbox_by_default: true,
            xhr_user_search: false
        });
        }
      });
           });
