package org.softeg.slartus.forpda;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import org.apache.http.cookie.Cookie;
import org.softeg.slartus.forpda.classes.*;
import org.softeg.slartus.forpda.classes.Exceptions.NotReportException;
import org.softeg.slartus.forpda.classes.common.Functions;
import org.softeg.slartus.forpda.common.Log;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 16.09.11
 * Time: 18:40
 * To change this template use File | Settings | File Templates.
 */
public class Client {

    public static final String SITE = "4pda.ru";
    public static final String MASTER_PASSWORD = "sx2AvOCPjXFhKoyWJMAqSiiPwaS2Z3Pc";
    private String m_User = "гость";

    private String m_K = "";

    private Client() {

    }

    public String getAuthKey() {
        return m_K;
    }

    public Forum MainForum = null;

    public static final Client INSTANCE = new Client();


    public URI getRedirectUri() {
        return HttpHelper.getRedirectUri();
    }

    public void setM_Logined(Boolean m_Logined) {
        this.m_Logined = m_Logined;
    }


    public void deletePost(String forumId, String themeId, String postId, CharSequence authKey) throws IOException {
        String res = performGet("http://4pda.ru/forum/index.php?act=Mod&CODE=04&f=" + forumId
                + "&t=" + themeId
                + "&p=" + postId
                + "&auth_key=" + authKey);

    }

    public String getEditPost(String forumId, String themeId, String postId, String authKey) throws Exception {
        String res = performGet("http://4pda.ru/forum/index.php?act=post&do=edit_post&f=" + forumId
                + "&t=" + themeId
                + "&p=" + postId
                + "&auth_key=" + authKey);

        String startFlag = "<textarea name=\"Post\" rows=\"8\" cols=\"150\" style=\"width:98%; height:160px\" tabindex=\"0\">";
        int startIndex = res.indexOf(startFlag);
        if (startIndex == -1) {
            Pattern pattern = Pattern.compile("<h4>Причина:</h4>\n" +
                    "\\s*\n" +
                    "\\s*<p>(.*)</p>", Pattern.MULTILINE);
            Matcher m = pattern.matcher(res);
            if (m.find()) {
                throw new NotReportException(m.group(1));
            }
            throw new NotReportException("Неизвестная причина");
        }
        startIndex += startFlag.length();
        int endIndex = res.indexOf("</textarea>", startIndex);
        return new String(res.substring(startIndex, endIndex));

    }

    public String getEditPostPlus(String forumId, String themeId, String postId, String authKey) throws Exception {
        String res = null;
        if (postId.equals("-1"))
            res = performGet("http://4pda.ru/forum/index.php?act=post&do=reply_post&f=" + forumId
                    + "&t=" + themeId);
        else
            res = performGet("http://4pda.ru/forum/index.php?act=post&do=edit_post&f=" + forumId
                    + "&t=" + themeId
                    + "&p=" + postId
                    + "&auth_key=" + authKey);

        String startFlag = "<textarea name=\"Post\" rows=\"8\" cols=\"150\" style=\"width:98%; height:160px\" tabindex=\"0\">";
        int startIndex = res.indexOf(startFlag);
        if (startIndex == -1) {
            Pattern pattern = Pattern.compile("<h4>Причина:</h4>\n" +
                    "\\s*\n" +
                    "\\s*<p>(.*)</p>", Pattern.MULTILINE);
            Matcher m = pattern.matcher(res);
            if (m.find()) {
                throw new NotReportException(m.group(1));
            }
            throw new NotReportException("Неизвестная причина");
        }
        return res;

    }

    public void editPost(String forumId, String themeId, String authKey, String postId, Boolean enablesig,
                         Boolean enableEmo, String post) throws IOException {

        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "Post");
        additionalHeaders.put("s", "");
        additionalHeaders.put("f", forumId);
        additionalHeaders.put("auth_key", authKey);
        additionalHeaders.put("removeattachid", "0");
        additionalHeaders.put("MAX_FILE_SIZE", "0");
        additionalHeaders.put("CODE", "09");
        additionalHeaders.put("t", themeId);
        additionalHeaders.put("p", postId);

        additionalHeaders.put("Post", post);
        if (enablesig)
            additionalHeaders.put("enablesig", "yes");
        if (enableEmo)
            additionalHeaders.put("enableemo", "yes");


        performPost("http://" + SITE + "/forum/index.php", additionalHeaders);

    }

    public String attachFilePost(String forumId, String themeId, String authKey, String attachPostKey, String postId, Boolean enablesig,Boolean enableEmo,
                                 String post, String filePath) throws Exception {

        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("st", "0");
        additionalHeaders.put("act", "Post");

        additionalHeaders.put("f", forumId);
        additionalHeaders.put("auth_key", authKey);
        additionalHeaders.put("removeattachid", "0");
        additionalHeaders.put("MAX_FILE_SIZE", "0");
        additionalHeaders.put("CODE", "03");
        additionalHeaders.put("t", themeId);

        additionalHeaders.put("attach_post_key", attachPostKey);

        additionalHeaders.put("parent_id", "0");
        additionalHeaders.put("ed-0_wysiwyg_used", "0");
        additionalHeaders.put("editor_ids[]", "ed-0");
        additionalHeaders.put("_upload_single_file", "1");
        additionalHeaders.put("upload_process", "Закачать");

        if (!postId.equals("-1"))
            additionalHeaders.put("p", postId);

        additionalHeaders.put("Post", post);
        if (enablesig)
            additionalHeaders.put("enablesig", "yes");
        if (enableEmo)
            additionalHeaders.put("enableEmo", "yes");

        HttpHelper httpHelper = new HttpHelper();
        String res = null;
        try {
            res = httpHelper.uploadFile("http://" + SITE + "/forum/index.php", filePath, additionalHeaders);
        } finally {
            httpHelper.close();
        }

        // m_HttpHelper.close();
        return res;

    }

    public String deleteAttachFilePost(String forumId, String themeId, String authKey, String attachPostKey, String postId,
                                       Boolean enablesig,
                                       String post, String attachToDeleteId) throws Exception {

        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("st", "0");
        additionalHeaders.put("act", "Post");

        additionalHeaders.put("f", forumId);
        additionalHeaders.put("auth_key", authKey);
        additionalHeaders.put("removeattachid", "0");
        additionalHeaders.put("MAX_FILE_SIZE", "0");
        additionalHeaders.put("CODE", "03");
        additionalHeaders.put("t", themeId);

        additionalHeaders.put("attach_post_key", attachPostKey);

        additionalHeaders.put("parent_id", "0");
        additionalHeaders.put("ed-0_wysiwyg_used", "0");
        additionalHeaders.put("editor_ids[]", "ed-0");
        additionalHeaders.put("_upload_single_file", "1");

        additionalHeaders.put("removeattach[" + attachToDeleteId + "]", "Удалить!");

        additionalHeaders.put("p", postId);

        additionalHeaders.put("Post", post);
        if (enablesig)
            additionalHeaders.put("enablesig", "yes");


        return performPost("http://" + SITE + "/forum/index.php", additionalHeaders);

    }

    public String changeReputation(String postId, String userId, String type, String message) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "rep");
        additionalHeaders.put("p", postId);
        additionalHeaders.put("mid", userId);
        additionalHeaders.put("type", type);
        additionalHeaders.put("message", message);

        String res = performPost("http://" + SITE + "/forum/index.php", additionalHeaders);

        Pattern p = Pattern.compile("<title>(.*?)</title>");
        Matcher m = p.matcher(res);
        if (m.find()) {
            if (m.group(1) != null && m.group(1).equals("Ошибка")) {
                p = Pattern.compile("<div class='maintitle'>(.*?)</div>");
                m = p.matcher(res);
                if (m.find()) {
                    return "Ошибка изменения репутации: " + m.group(1);
                }
                return "Ошибка изменения репутации";
            }
            return "Репутация: " + m.group(1);
        }
        return "Репутация изменена";
    }

    public String claim(String themeId, String postId, String message) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "report");
        additionalHeaders.put("send", "1");
        additionalHeaders.put("t", themeId);
        additionalHeaders.put("p", postId);
        additionalHeaders.put("message", message);

        String res = performPost("http://4pda.ru/forum/index.php?act=report&amp;send=1&amp;t=" + themeId + "&amp;p=" + postId, additionalHeaders);

        Pattern p = Pattern.compile("<div class=\"errorwrap\">\n" +
                "\\s*<h4>Причина:</h4>\n" +
                "\\s*\n" +
                "\\s*<p>(.*)</p>", Pattern.MULTILINE);
        Matcher m = p.matcher(res);
        if (m.find()) {

            return "Ошибка отправки жалобы: " + m.group(1);
        }
        return "Жалоба отправлена";
    }

    public String themeSubscribe(Topic topic, String emailtype) throws IOException {
        if (TextUtils.isEmpty(topic.getForumId()) || TextUtils.isEmpty(topic.getAuthKey())) {
            setThemeForumAndAuthKey(topic);
        }
        if (TextUtils.isEmpty(topic.getForumId())) {
            return "Не могу получить идентификатор форума для темы";
        }
        if (TextUtils.isEmpty(topic.getAuthKey())) {
            return "Не могу получить ключ авторизации";
        }

        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "usercp");
        additionalHeaders.put("CODE", "end_subs");
        additionalHeaders.put("method", "topic");
        additionalHeaders.put("auth_key", topic.getAuthKey());
        additionalHeaders.put("tid", topic.getId());
        additionalHeaders.put("fid", topic.getForumId());
        additionalHeaders.put("st", "0");
        additionalHeaders.put("emailtype", emailtype);
        String res = performPost("http://" + SITE + "/forum/index.php", additionalHeaders);

        Pattern p = Pattern.compile("<div class=\"errorwrap\">\n" +
                "\\s*<h4>Причина:</h4>\n" +
                "\\s*\n" +
                "\\s*<p>(.*)</p>", Pattern.MULTILINE);
        Matcher m = p.matcher(res);
        if (m.find()) {

            return "Ошибка подписки: " + m.group(1);
        }
        return "Подписка оформлена";
    }

    public String unSubscribe(Topic topic) throws IOException {
        String body = performGet("http://" + SITE + "/forum/index.php?act=UserCP&CODE=26");

        Pattern pattern = Pattern.compile("(<td colspan=\"6\" class=\"row1\"><b>(.*?)</b></td>)?\n" +
                "\\s*</tr><tr>\n" +
                "\\s*<td class=\"row2\" align=\"center\" width=\"5%\">(<font color='.*?'>)?(.*?)(</font>)?</td>\n" +
                "\\s*<td class=\"row2\">\n" +
                "\\s*<a href=\"http://4pda.ru/forum/index.php\\?showtopic=" + topic.getId() + "\">(.*?)</a>&nbsp;\n" +
                "\\s*\\( <a href=\"http://4pda.ru/forum/index.php\\?showtopic=" + topic.getId() + "\" target=\"_blank\">В новом окне</a> \\)\n" +
                "\\s*<div class=\"desc\">((.*?)<br />)?.*?\n" +
                "\\s*<br />\n" +
                "\\s*Тип: .*?\n" +
                "\\s*</div>\n" +
                "\\s*</td>\n" +
                "\\s*<td class=\"row2\" align=\"center\"><a href=\"javascript:who_posted\\(\\d+\\);\">(\\d+)</a></td>\n" +
                "\\s*<td class=\"row2\" align=\"center\">\\d+</td>\n" +
                "\\s*<td class=\"row2\">(.*?)<br />автор: <a href='http://4pda.ru/forum/index.php\\?showuser=(\\d+)'>(.*?)</a></td>" +
                "\\s*<td class=\"row1\" align=\"center\" style='padding: 1px;'><input class='checkbox' type=\"checkbox\" name=\"id-(\\d+)\" value=\"yes\" /></td>\n");

        Matcher m = pattern.matcher(body);
        body = null;
        if (m.find()) {
            Map<String, String> additionalHeaders = new HashMap<String, String>();
            additionalHeaders.put("act", "UserCP");
            additionalHeaders.put("CODE", "27");
            additionalHeaders.put("id-" + m.group(13), "yes");
            additionalHeaders.put("trackchoice", "unsubscribe");
            performPost("http://" + SITE + "/forum/index.php", additionalHeaders);

            return "Вы отписались от темы";
        } else {
            throw new NotReportException("Тема в подписках не найдена");
        }
    }

    public void clearCookies() {
        HttpHelper httpHelper = new HttpHelper();
        try {
            httpHelper.clearCookies();
        } finally {
            httpHelper.close();
        }


    }

    public Boolean hasLoginCookies() {
        Boolean session = false;
        Boolean pass_hash = false;
        Boolean member = false;
        HttpHelper httpHelper = new HttpHelper();
        try {
            List<Cookie> cookies = httpHelper.getCookies();
            for (Cookie cookie : cookies) {
                if (!session && cookie.getName().equals("session_id"))
                    session = true;
                else if (!pass_hash && cookie.getName().equals("pass_hash"))
                    pass_hash = true;
                else if (!member && cookie.getName().equals("member_id"))
                    member = true;
            }
        } finally {
            httpHelper.close();
        }
        return session && pass_hash && member;
    }

    public String performGet(String s) throws IOException {
        HttpHelper httpHelper = new HttpHelper();
        String res = null;
        try {
            // s="http://4pda.ru/2009/12/28/18506/#comment-363525";
            res = httpHelper.performGet(s);
        } finally {
            httpHelper.close();
        }
        // m_HttpHelper.close();
        return res;
    }


    public String performPost(String s, Map<String, String> additionalHeaders) throws IOException {
        HttpHelper httpHelper = new HttpHelper();
        String res = null;
        try {
            // s="http://4pda.ru/2009/12/28/18506/#comment-363525";
            res = httpHelper.performPost(s, additionalHeaders);
            //  m_HttpHelper.close();
        } finally {
            httpHelper.close();
        }
        return res;
    }

    public String performPost(String s, Map<String, String> additionalHeaders, String encoding) throws IOException {
        HttpHelper httpHelper = new HttpHelper();
        String res = null;
        try {
            // s="http://4pda.ru/2009/12/28/18506/#comment-363525";
            res = httpHelper.performPost(s, additionalHeaders, encoding);
            //  m_HttpHelper.close();
        } finally {
            httpHelper.close();
        }
        return res;
    }


    public interface OnUserChangedListener {
        void onUserChanged(String user, Boolean success);
    }

    public void doOnUserChangedListener(String user, Boolean success) {
        for (OnUserChangedListener listener : m_OnUserChangeListeners) {
            listener.onUserChanged(user, success);
        }

    }

    private List<OnUserChangedListener> m_OnUserChangeListeners = new ArrayList<OnUserChangedListener>();

    public void addOnUserChangedListener(OnUserChangedListener p) {
        m_OnUserChangeListeners.add(p);

    }

    public interface OnMailListener {
        void onMail(int count);
    }

    public void doOnMailListener(int count) {
        for (OnMailListener listener : m_OnMailListeners) {
            listener.onMail(count);
        }

    }

    private List<OnMailListener> m_OnMailListeners = new ArrayList<OnMailListener>();

    public void addOnMailListener(OnMailListener p) {
        m_OnMailListeners.add(p);

    }


    public interface OnProgressChangedListener {
        void onProgressChanged(String state);

    }

    public interface OnProgressPositionChangedListener {
        void onProgressChanged(int state, Exception ex);

    }


    public void doOnOnProgressChanged(OnProgressChangedListener listener, String state) {
        if (listener != null) {
            listener.onProgressChanged(state);
        }
    }

    private AlertDialog m_LoginDialog = null;

    public void showLoginForm(Context mContext, OnUserChangedListener onUserChangedListener) {
        try {
            // if (m_LoginDialog == null)
            {
                final Context context = mContext;
                final OnUserChangedListener monUserChangedListener = onUserChangedListener;
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.login_activity, null);

                final EditText username_edit = (EditText) layout.findViewById(R.id.username_edit);
                final EditText password_edit = (EditText) layout.findViewById(R.id.password_edit);
                final CheckBox privacy_checkbox = (CheckBox) layout.findViewById(R.id.privacy_checkbox);
                final CheckBox remember_checkbox = (CheckBox) layout.findViewById(R.id.remember_checkbox);
                final CheckBox autologin_checkbox = (CheckBox) layout.findViewById(R.id.autologin_checkbox);


                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                username_edit.setText(preferences.getString("Login", ""));
                privacy_checkbox.setChecked(preferences.getBoolean("LoginPrivacy", false));
                remember_checkbox.setChecked(preferences.getBoolean("LoginRemember", true));
                autologin_checkbox.setChecked(preferences.getBoolean("AutoLogin", true));


                m_LoginDialog = new AlertDialog.Builder(context)
                        .setTitle("Вход")
                        .setView(layout)
                        .setPositiveButton("Вход", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                LoginTask loginTask = new LoginTask(context);
                                loginTask.setOnUserChangedListener(monUserChangedListener);
                                loginTask.execute(username_edit.getText().toString(), password_edit.getText().toString(),
                                        Boolean.toString(privacy_checkbox.isChecked()),
                                        Boolean.toString(remember_checkbox.isChecked()), Boolean.toString(autologin_checkbox.isChecked())
                                );
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (monUserChangedListener != null)
                                    monUserChangedListener.onUserChanged(m_User, false);
                            }
                        })
                        .create();
            }

            m_LoginDialog.show();
        } catch (Exception ex) {
            Log.e(mContext, ex);
        }
    }


    public String getUser() {
        return m_User;
    }

    private Boolean m_Logined = false;

    public Boolean getLogined() {
        return m_Logined;
    }


    private String m_LoginFailedReason;

    public String getLoginFailedReason() {
        return m_LoginFailedReason;
    }

    public String reply(String forumId, String themeId, String authKey, String post,
                        Boolean enablesig, Boolean enableemo) throws IOException {
        return reply(forumId, themeId, authKey, null, post,
                enablesig, enableemo);

    }

    public String reply(String forumId, String themeId, String authKey, String attachPostKey, String post,
                        Boolean enablesig, Boolean enableemo) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "Post");
        additionalHeaders.put("CODE", "03");
        additionalHeaders.put("f", forumId);
        additionalHeaders.put("t", themeId);
        additionalHeaders.put("fast_reply_used", "1");
        additionalHeaders.put("auth_key", authKey);
        if (!TextUtils.isEmpty(attachPostKey))
            additionalHeaders.put("attach_post_key", attachPostKey);
        additionalHeaders.put("Post", post);
        if (enablesig)
            additionalHeaders.put("enablesig", "yes");
        if (enableemo)
            additionalHeaders.put("enableemo", "yes");


        additionalHeaders.put("referer", "http://" + SITE + "/forum/index.php?act=Post&CODE=03&f=" + forumId + "&t=" + themeId + "&st=20&auth_key=" + authKey + "&fast_reply_used=1");

        String res = performPost("http://" + SITE + "/forum/index.php", additionalHeaders);
        Pattern checkPattern = Pattern.compile("\t\t<h4>Причина:</h4>\n" +
                "\n" +
                "\t\t<p>(.*?)</p>", Pattern.MULTILINE);
        Matcher m = checkPattern.matcher(res);
        if (m.find()) {
            return m.group(1);
        }

        checkPattern = Pattern.compile("<div class=\".*?\">(<b>)?ОБНАРУЖЕНЫ СЛЕДУЮЩИЕ ОШИБКИ(</b>)?</div>\n" +
                "\\s*<div class=\".*?\">(.*?)</div>", Pattern.MULTILINE);
        m = checkPattern.matcher(res);
        if (m.find()) {
            return Html.fromHtml(m.group(3)).toString();
        }

        return null;


    }

    private String m_SessionId;

    public Boolean login(String login, String password, Boolean privacy) throws Exception {
        m_LoginFailedReason = null;

        Map<String, String> additionalHeaders = new HashMap<String, String>();

        additionalHeaders.put("UserName", login.replace(" ", "\\ "));
        additionalHeaders.put("PassWord", password);
        additionalHeaders.put("CookieDate", "1");
        additionalHeaders.put("Privacy", privacy ? "1" : "0");
        UUID uuid = UUID.randomUUID();

        m_SessionId = uuid.toString().replace("-", "");
        additionalHeaders.put("s", m_SessionId);
        additionalHeaders.put("act", "Login");
        additionalHeaders.put("CODE", "01");

        additionalHeaders.put("referer", "http://" + SITE + "/forum/index.php?s=" + m_SessionId + "&amp;amp;s=" + m_SessionId + "&amp;act=Login&amp;CODE=01");

        HttpHelper httpHelper = new HttpHelper();
        String res;
        try {


            res = httpHelper.performPost("http://" + SITE + "/forum/index.php", additionalHeaders);

            if (TextUtils.isEmpty(res)) {
                m_LoginFailedReason = "Сервер вернул пустую страницу";
                return false;
            }
            checkLogin(res);


            if (!m_Logined) {
                Pattern checkPattern = Pattern.compile("\t\t<h4>Причина:</h4>\n" +
                        "\n" +
                        "\t\t<p>(.*?)</p>", Pattern.MULTILINE);
                Matcher m = checkPattern.matcher(res);
                if (m.find()) {
                    m_LoginFailedReason = m.group(1);
                } else {
                    checkPattern = Pattern.compile("\t<div class=\"formsubtitle\">Обнаружены следующие ошибки:</div>\n" +
                            "\t<div class=\"tablepad\"><span class=\"postcolor\">(.*?)</span></div>");
                    m = checkPattern.matcher(res);
                    if (m.find()) {
                        m_LoginFailedReason = m.group(1);
                    } else {
                        m_LoginFailedReason = Html.fromHtml(res).toString();
                    }

                }

            }


            httpHelper.writeExternalCookies();
        } finally {
            httpHelper.close();
        }
        return m_Logined;
    }

    private final Pattern checkLoginPattern = Pattern.compile("<a href=\"(http://4pda.ru)?/forum/index.php\\?showuser=(\\d+)\">(.*?)</a></b> \\( <a href=\"(http://4pda.ru)?/forum/index.php\\?act=Login&amp;CODE=03&amp;k=([a-z0-9]{32})\">Выход</a>");

    public void checkLogin(String pageBody) {
//        List<Cookie> cookies = m_HttpHelper.getCookies();
//        Cookie memberCookie = null;
//        Cookie pasHashCookie = null;
//        Date nowDate = new Date();
//        for (int i = 0; i < cookies.size(); i++) {
//            Cookie cookie = cookies.get(i);
//            if (memberCookie == null && "member_id".equals(cookie.getName()) && !cookie.isExpired(nowDate))
//                memberCookie = cookie;
//            if (pasHashCookie == null && "pass_hash".equals(cookie.getName()) && !cookie.isExpired(nowDate))
//                pasHashCookie = cookie;
//            if (pasHashCookie != null && memberCookie != null) break;
//        }
//        if (!"deleted".equals(memberCookie.getValue()) && !"deleted".equals(pasHashCookie.getValue())) {
//            m_User = memberCookie.getValue();
//            m_Logined = true;
//        }else{
//            m_Logined = false;
//            m_User = "гость";
//        }

        Matcher m = checkLoginPattern.matcher(pageBody);
        if (m.find()) {
            m_User = m.group(3);
            m_K = m.group(5);
            m_Logined = true;
        } else {
            m_Logined = false;
            m_User = "гость";
        }
        doOnUserChangedListener(m_User, m_Logined);
    }

    private int m_MailsCount = 0;

    public int getMailsCount() {
        return m_MailsCount;
    }

    private String m_Qms = null;

    public String getQms() {
        return m_Qms;
    }

    public void checkMails(String pageBody) {
        final Pattern qmsPattern = Pattern.compile("QMS:([\\s\\S]*?)<style");
        final Pattern newMessages = Pattern.compile("/forum/index.php\\?act=Msg&amp;CODE=01\">((\\d+) новых писем)|(Новых писем: (\\d+))</a>");
        Matcher m = newMessages.matcher(pageBody);
        m_MailsCount = 0;
        if (m.find()) {
            String s=m.group(2)==null?m.group(4):m.group(2);
            m_MailsCount = Integer.parseInt(s);

        }

        m = qmsPattern.matcher(pageBody);
        m_Qms = null;
        if (m.find()) {
            final Pattern qmsSendersPattern = Pattern.compile("\">(.*?)</a>");
            Matcher m1 = qmsSendersPattern.matcher(m.group(1));
            String senders = "";
            while (m1.find()) {
                senders += m1.group(1) + ",";
            }
            m_Qms = senders;

        }
        doOnMailListener(m_MailsCount);
    }

    public Boolean logout() throws IOException {

        String res = performGet("http://4pda.ru/forum/index.php?act=Login&CODE=03&k=" + m_K);

        checkLogin(res);
        if (m_Logined)
            m_LoginFailedReason = "Неудачный выход";

        return !m_Logined;
    }

    public void loadForums(OnProgressChangedListener progressChangedListener) throws Exception {

        MainForum = new Forum("-1", "4PDA");
        doOnOnProgressChanged(progressChangedListener, "Получение данных...");
        String pageBody = performGet("http://" + SITE + "/forum/lofiversion/index.php");

        doOnOnProgressChanged(progressChangedListener, "Обработка данных...");

        String[] strings = pageBody.split("\n");
        pageBody = null;
        Pattern checkRegimePattern = Pattern.compile("<div id='largetext'>Полная версия этой страницы");
        Pattern forumPattern = Pattern.compile("<a href='http://" + SITE + "/forum/lofiversion/index.php\\?f(\\d+).html'>(.*?)</a>");

        Forum forum = MainForum;
        Boolean regimeChecked = false;
        for (String str : strings) {
            regimeChecked = regimeChecked || checkRegimePattern.matcher(str).find();
            Matcher m = forumPattern.matcher(str);
            if (m.find()) {
                if (forum.getParent() != null && forum.getParent() != MainForum && forum.getForums().size() == 0)
                    forum.addForum(new Forum(forum.getId(), forum.getTitle() + " @ темы"));
                forum.addForum(new Forum(m.group(1), Html.fromHtml(m.group(2)).toString()));
            } else if (str.endsWith("<ul>")) {
                forum = forum.getLastChild();
            } else if (str.trim().startsWith("</ul></li>")) {
                forum = forum.getParent();
                if (forum == null)
                    forum = MainForum;
            }
        }
        if (!regimeChecked)
            throw new NotReportException("Страница загрузилась не в текстовом режиме! Попробуйте залогиниться");

    }

    public String loadPageAndCheckLogin(String url, OnProgressChangedListener progressChangedListener) throws IOException {
        doOnOnProgressChanged(progressChangedListener, "Получение данных...");
        String body = performGet(url);
        doOnOnProgressChanged(progressChangedListener, "Обработка данных...");

        Matcher headerMatcher = Pattern.compile("<body>([\\s\\S]*?)globalmess").matcher(body);
        if (headerMatcher.find()) {
            checkLogin(headerMatcher.group(1));
            checkMails(headerMatcher.group(1));
        } else {
            checkLogin(body);
            checkMails(body);
        }
        return body;
    }

    public void loadUserReputation(Reputations res, OnProgressChangedListener progressChangedListener) throws IOException {

        String body = loadPageAndCheckLogin("http://4pda.ru/forum/index.php?act=rep&type=history&mid=" + res.userId + "&st=" + res.size(), progressChangedListener);

        Pattern pattern = Pattern.compile("<div class='maintitle'>(.*?)<div");
        Matcher m = pattern.matcher(body);
        if (m.find())
            res.description = m.group(1);

        if (res.fullListCount == 0) {
            pattern = Pattern.compile("parseInt\\((\\d+)/\\d+\\)");
            m = pattern.matcher(body);
            if (m.find())
                res.fullListCount = Integer.parseInt(m.group(1));
        }

        pattern = Pattern.compile("\\s*<td class='row2' align='left'><b><a href='http://4pda.ru/forum/index.php\\?showuser=(\\d+)'>(.*)</a></b></td>\n" +
                "\\s*<td class='row2' align='left'>(<b>)?<a href='(.*)'>(.*?)</a>(</b>)?</td>\n" +
                "\\s*<td class='row2' align='left'>(.*?)</td>\n" +
                "\\s*<td class='row1' align='center'><img border='0' src='style_images/1/(.*?).gif' /></td>\n" +
                "\\s*<td class='row1' align='center'>(.*)</td>", Pattern.MULTILINE);
        m = pattern.matcher(body);

        while (m.find()) {
            Reputation rep = new Reputation();
            rep.userId = m.group(1);
            rep.user = m.group(2);
            rep.sourceUrl = m.group(4);
            rep.source = Html.fromHtml(m.group(5));
            rep.description = Html.fromHtml(m.group(7));
            rep.level = m.group(8);
            rep.date = m.group(9);
            res.add(rep);
        }


    }


    public TopicBodyBuilder loadTopic(Handler handler, Context context, String themeUrl, Boolean spoilFirstPost, Boolean enableSig,
                                      Boolean enableEmo, String postBody, Boolean hidePostForm,
                                      OnProgressChangedListener progressChangedListener) throws IOException, Topic.ThemeParseException {
//        if (progressChangedListener != null)
//            doOnOnProgressChanged(progressChangedListener, "Получение данных...");
        // themeUrl=java.net.URLEncoder.encode(themeUrl, "windows-1251");
        String res = loadPageAndCheckLogin("http://" + SITE + "/forum/index.php?" + themeUrl,progressChangedListener);
//        if (progressChangedListener != null)
//            doOnOnProgressChanged(progressChangedListener, "Обработка данных...");

        checkLogin(res);

        Pattern pattern = Pattern.compile("showtopic=(\\d+)(&(.*))?");
        Matcher m = pattern.matcher(themeUrl);
        String topicId = null;
        String urlParams = null;
        if (m.find()) {

            topicId = m.group(1);

            urlParams = m.group(3);
        }

        TopicBodyBuilder topicBodyBuilder = loadTopic(handler, context, topicId, res, spoilFirstPost, m_Logined,
                urlParams, enableSig, enableEmo, postBody,  hidePostForm);
        return topicBodyBuilder;
    }

    public String loadThemeBody(String themeUrl, OnProgressChangedListener progressChangedListener) throws IOException {
        if (progressChangedListener != null)
            doOnOnProgressChanged(progressChangedListener, "Получение данных...");
        // themeUrl=java.net.URLEncoder.encode(themeUrl, "windows-1251");
        String res = performGet("http://" + SITE + "/forum/index.php?" + themeUrl);

        checkLogin(res);


        return res;
    }

    public void getFavoritesThemes(Themes themes, OnProgressChangedListener progressChangedListener) throws IOException {

        String pageBody = loadPageAndCheckLogin("http://" + SITE + "/forum/index.php?autocom=favtopics&st=" + themes.size(), progressChangedListener);


        Pattern pattern = Pattern.compile("<a id=\"tid-link-\\d+\" href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+)\" title=\".*?\">(.*?)</a></span>");
        Pattern descPattern = Pattern.compile(" id='tid-desc-\\d+'>(.*?)</span>");
        Pattern postsCountPattern = Pattern.compile("<a href=\"javascript:who_posted\\(\\d+\\);\">(\\d+)</a>");
        //Pattern authorPattern = Pattern.compile("<td align=\"center\" class=\"row2\"><a href='http://4pda.ru/forum/index.php\\?showuser=(\\d+)'>(.*?)</a></td>");
        Pattern lastMessageDatePattern = Pattern.compile("<span class=\"lastaction\">(.*?)<br /><a href=\"http://4pda.ru/forum/index.php\\?showtopic=\\d+&amp;view=getlastpost\">Послед.:</a> <b><a href='http://4pda.ru/forum/index.php\\?showuser=(\\d+)'>(.*?)</a>");

        Pattern pagesCountPattern = Pattern.compile("<a href=\"http://4pda.ru/forum/index.php\\?autocom=.*?st=(\\d+)\">&raquo;</a>");


        String[] strings = pageBody.split("\n");
        pageBody = null;
        int phase = 0;
        Matcher m;
        Topic topic = null;

        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();
        for (String str : strings) {
            if (themes.getThemesCount() == 0) {
                m = pagesCountPattern.matcher(str);
                if (m.find()) {
                    themes.setThemesCountInt(Integer.parseInt(m.group(1)) + 1);
                }
            }
            switch (phase) {
                case 0:
                    m = pattern.matcher(str);
                    if (m.find()) {
                        topic = new Topic(m.group(1), m.group(2));
                        topic.setIsNew(str.contains("view=getnewpost"));
                        phase++;
                    }
                    break;
                case 1:
                    m = descPattern.matcher(str);
                    if (m.find()) {
                        topic.setDescription(m.group(1));
                        phase++;
                    }
                    break;
                case 2:
                    m = postsCountPattern.matcher(str);
                    if (m.find()) {
                        topic.setPostsCount(m.group(1));
                        phase++;
                    }
                    break;
                case 3:
                    m = lastMessageDatePattern.matcher(str);
                    if (m.find()) {
                        topic.setLastMessageDate(Functions.parseForumDateTime(m.group(1), today, yesterday));
                        topic.setLastMessageAuthorId(m.group(2));
                        topic.setLastMessageAuthor(m.group(3));
                        themes.add(topic);
                        phase = 0;
                    }
                    break;
            }
        }

    }


    public void getSubscribes(Themes themes, OnProgressChangedListener progressChangedListener) throws IOException {


        String pageBody = loadPageAndCheckLogin("http://" + SITE + "/forum/index.php?act=UserCP&CODE=26", progressChangedListener);

        Pattern pattern = Pattern.compile("(<td colspan=\"6\" class=\"row1\"><b>(.*?)</b></td>)?\n" +
                "\\s*</tr><tr>\n" +
                "\\s*<td class=\"row2\" align=\"center\" width=\"5%\">(<font color='.*?'>)?(.*?)(</font>)?</td>\n" +
                "\\s*<td class=\"row2\">\n" +
                "\\s*<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+).*?\">(.*?)</a>&nbsp;\n" +
                "\\s*\\( <a href=\"http://4pda.ru/forum/index.php\\?showtopic=\\d+.*?\" target=\"_blank\">В новом окне</a> \\)\n" +
                "\\s*<div class=\"desc\">((.*?)<br />)?.*?\n" +
                "\\s*<br />\n" +
                "\\s*Тип: .*?\n" +
                "\\s*</div>\n" +
                "\\s*</td>\n" +
                "\\s*<td class=\"row2\" align=\"center\"><a href=\"javascript:who_posted\\(\\d+\\);\">(\\d+)</a></td>\n" +
                "\\s*<td class=\"row2\" align=\"center\">\\d+</td>\n" +
                "\\s*<td class=\"row2\">(.*?)<br />автор: <a href='http://4pda.ru/forum/index.php\\?showuser=(\\d+)'>(.*?)</a></td>");


        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();


        Matcher m = pattern.matcher(pageBody);
        String forumTitle = null;
        while (m.find()) {

            if (!TextUtils.isEmpty(m.group(2)))
                forumTitle = m.group(2);
            Topic topic = new Topic(m.group(6), m.group(7));
            topic.setDescription(m.group(9));
            topic.setForumTitle(forumTitle);
            topic.setPostsCount(m.group(10));
            topic.setLastMessageDate(Functions.parseForumDateTime(m.group(11), today, yesterday));
            topic.setLastMessageAuthorId(m.group(12));
            topic.setLastMessageAuthor(m.group(13));
            topic.setIsNew(m.group(4).equals("+"));
            themes.add(topic);
        }

    }


    private Topic createFullVersionTopic(String id, String page) {

        final Pattern forumIdPattern = Pattern.compile("name=\"forums\\[\\]\" value=\"(\\d+)\"");
        final Pattern userPattern = Pattern.compile("<a href=\"(http://4pda.ru)?/forum/index.php\\?showuser=\\d+\">.*?</a></b> \\( <a href=\"(http://4pda.ru)?/forum/index.php\\?act=Login&amp;CODE=03&amp;k=([a-z0-9]{32})\">Выход</a>");
        final Pattern titlePattern = Pattern.compile("<img src=\"http://s.4pda.ru/forum/style_images/1/nav_m.gif\" border=\"0\"  alt=\"&gt;\" width=\"8\" height=\"8\" />(.*?)</div>");
        final Pattern moderatorTitlePattern = Pattern.compile("onclick=\"return setpidchecks\\(this.checked\\);\".*?>&nbsp;(.*?)<");
        final Pattern pagesCountPattern = Pattern.compile("id=\"page-jump\">(\\d+) страниц");
        final Pattern lastPageStartPattern = Pattern.compile("(http://4pda.ru)?/forum/index.php\\?showtopic=\\d+&amp;st=(\\d+)");
        final Pattern currentPagePattern = Pattern.compile("<span class=\"pagecurrent\">(\\d+)</span>");
        String str = page;

        String title = null;
        Matcher m = titlePattern.matcher(str);
        if (m.find()) {
            title = m.group(1);
        } else {
            m = moderatorTitlePattern.matcher(str);
            if (m.find())
                title = m.group(1);
        }

        Topic topic = new Topic(id, title);

        m = forumIdPattern.matcher(str);
        if (m.find()) {
            topic.setForumId(m.group(1));
        }

        m = userPattern.matcher(str);
        if (m.find()) {
            topic.setAuthKey(m.group(3));
        }

        m = pagesCountPattern.matcher(str);
        if (m.find()) {
            topic.setPagesCount(Integer.toString(Integer.parseInt(m.group(1).toString()) - 1));
        }

        m = lastPageStartPattern.matcher(str);
        while (m.find()) {
            topic.setLastPageStartCount(m.group(2));
        }

        m = currentPagePattern.matcher(str);
        if (m.find()) {
            topic.setCurrentPage(m.group(1));
        }
        return topic;

    }

    private Topic createTopic(Handler handler, final Context context, String id, String page) {


        final Pattern navStripPattern = Pattern.compile("<div id=\"navstrip\">(.*?)</div>");
        final Pattern userPattern = Pattern.compile("<b><a href=\"(http://4pda.ru)?/forum/index.php\\?showuser=\\d+\">.*?</a></b> \\( <a href=\"(http://4pda.ru)?/forum/index.php\\?act=Login&amp;CODE=03&amp;k=([a-z0-9]{32})\">Выход</a>");
        final Pattern titlePattern = Pattern.compile("<div class=\"topic_title_post\">(.*?)<");
        final Pattern moderatorTitlePattern = Pattern.compile("onclick=\"return setpidchecks\\(this.checked\\);\".*?>&nbsp;(.*?)<");
        final Pattern pagesCountPattern = Pattern.compile("var pages = parseInt\\((\\d+)\\);");
        final Pattern lastPageStartPattern = Pattern.compile("(http://4pda.ru)?/forum/index.php\\?showtopic=\\d+&amp;st=(\\d+)");
        final Pattern currentPagePattern = Pattern.compile("<span class=\"pagecurrent\">(\\d+)</span>");
        String str = page;

        String title = null;
        Matcher m = titlePattern.matcher(str);
        if (m.find()) {
            title = m.group(1);
        } else {
            m = moderatorTitlePattern.matcher(str);
            if (m.find())
                title = m.group(1);
        }

        Topic topic = new Topic(id, title);


        m = navStripPattern.matcher(str);
        if (m.find()) {
            final Pattern forumPatter = Pattern.compile("<a href=\"(http://4pda.ru)?/forum/index.php\\?.*?showforum=(\\d+).*?\">(.*?)</a>");
            Matcher forumMatcher = forumPatter.matcher(m.group(1));
            while (forumMatcher.find()) {
                topic.setForumId(forumMatcher.group(2));
                topic.setForumTitle(forumMatcher.group(3));
            }
        }

        m = userPattern.matcher(str);
        if (m.find()) {
            topic.setAuthKey(m.group(3));
        }

        m = pagesCountPattern.matcher(str);
        if (m.find()) {
            topic.setPagesCount(m.group(1));
        }

        m = lastPageStartPattern.matcher(str);
        while (m.find()) {
            topic.setLastPageStartCount(m.group(2));
        }

        m = currentPagePattern.matcher(str);
        if (m.find()) {
            topic.setCurrentPage(m.group(1));
        } else
            topic.setCurrentPage("1");
        return topic;

    }

    public TopicBodyBuilder loadFullVersionTopic(String id, Matcher mainMatcher, Boolean spoilFirstPost,
                                                 Boolean logined, String urlParams, Boolean enableSig,
                                                 Boolean enableEmo, String postBody, Boolean hidePostForm,
                                                 Boolean isWebviewAllowJavascriptInterface) throws IOException, Topic.ThemeParseException {

        Topic topic = createFullVersionTopic(id, mainMatcher.group(1));

        TopicBodyBuilder topicBodyBuilder = new TopicBodyBuilder(logined, topic, urlParams, enableSig,
                enableEmo, postBody,  hidePostForm,isWebviewAllowJavascriptInterface);
        topicBodyBuilder.beginTopic();
        final Pattern postPattern = Pattern.compile("<table class=\"ipbtable\" cellspacing=\"1\">([\\s\\S]*?)((<!--Begin Msg Number)|(<!-- TABLE))", Pattern.MULTILINE);


        Matcher matcher = postPattern.matcher(mainMatcher.group(2) + "<!-- TABLE");
        final Pattern fullPattern = Pattern.compile(
                "<tr.*?>[\\s\\S]*?" +
                        "<td.*?>[\\s\\S]*?<a name=\"entry(\\d+)\">[\\s\\S]*?<span class=\"normalname\"><a href=\"/forum/index.php\\?showuser=(\\d+)\">(.*?)</a></span>[\\s\\S]*?" +
                        "</td>[\\s\\S]*?" +
                        "<td.*?>[\\s\\S]*?/>(.*?)</span>[\\s\\S]*?#(\\d+)[\\s\\S]*?" +
                        "</td>[\\s\\S]*?" +
                        "</tr>[\\s\\S]*?" +
                        "<tr.*?>[\\s\\S]*?" +
                        "<td.*?>[\\s\\S]*?\\[(offline|online)\\][\\s\\S]*?Группа:&nbsp;<span.*?>(.*?)</span>[\\s\\S]*?<a href=\"/forum/index.php\\?act=rep.*?>(\\d+)</a>([\\s\\S]*?)" +
                        "</td>[\\s\\S]*?" +
                        "<td.*?>([\\s\\S]*)");

        final Pattern bodyPattern = Pattern.compile("<div class=\"postcolor\".*?>([\\s\\S]*?)<tr id=\"pb-\\d+-r3\">([\\s\\S]*)");
        final Pattern pattern = Pattern.compile("^([\\s\\S]*)</div>");
        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();
        Post post = null;
        Boolean spoil = spoilFirstPost;
        while (matcher.find()) {
            if (post != null) {
                topicBodyBuilder.addPost(post, spoil);
                spoil = false;
            }
            Matcher m = fullPattern.matcher(matcher.group(1));
            if (m.find()) {
                post = new Post(m.group(1), Functions.getForumDateTime(Functions.parseForumDateTime(m.group(4), today, yesterday)), m.group(5));
                post.setAuthor(m.group(3));
                post.setUserGroup(m.group(7));
                post.setUserState(m.group(6));
                post.setUserId(m.group(2));
                post.setUserReputation(m.group(8));
                // !TODO:
                post.setCanPlusRep(m.group(9).contains("Поднять репутацию"));
                post.setCanMinusRep(post.getCanPlusRep());

                Matcher m1 = bodyPattern.matcher(m.group(10));
                if (m1.find()) {
                    Matcher m2 = pattern.matcher(m1.group(1));
                    if (m2.find())
                        post.setBody(m2.group(1));
                    post.setCanEdit(m1.group(2).contains("alt=\"Редактировать\""));
                    post.setCanDelete(m1.group(2).contains("alt=\"Удалить\""));
                }

                // topic.addMessage(post);
            }

        }
        if (post != null) {
            topicBodyBuilder.addPost(post, spoil);
            spoil = false;
        }

        topicBodyBuilder.endTopic();


        return topicBodyBuilder;
    }

    public TopicBodyBuilder loadTopic(Handler handler, Context context,
                                      String id, String topicBody, Boolean spoilFirstPost,
                                      Boolean logined, String urlParams, Boolean enableSig,
                                      Boolean enableEmo, String postBody, Boolean hidePostForm) throws IOException, Topic.ThemeParseException {
        final Pattern errorPattern = Pattern.compile("<div class=\"errorwrap\">([\\s\\S]*?)</div>");
        Matcher errorMatcher = errorPattern.matcher(topicBody);
        if (errorMatcher.find()) {
            final Pattern errorReasonPattern = Pattern.compile("<p>(.*?)</p>");
            Matcher errorReasonMatcher = errorReasonPattern.matcher(errorMatcher.group(1));
            if (errorReasonMatcher.find()) {
                throw new NotReportException(errorReasonMatcher.group(1));
            }
        }

        final Pattern headerPattern = Pattern.compile("^([\\s\\S]*?)<!--Begin Msg Number([\\s\\S]*?)<!-- TABLE([\\s\\S]*)");
        Matcher mainMatcher = headerPattern.matcher(topicBody);
        if (!mainMatcher.find()) {
            throw new IOException("Ошибка разбора страницы id=" + id);
        }

        Boolean isWebviewAllowJavascriptInterface=Functions.isWebviewAllowJavascriptInterface(context);
        Boolean isFullVersion = mainMatcher.group(3).contains("<a href=\"/wp-content/plugins/ngx.php?mb=1\"><b>Мобильная версия</b></a>");
        if (isFullVersion) {
            return loadFullVersionTopic(id, mainMatcher, spoilFirstPost, logined, urlParams, enableSig,
                    enableEmo, postBody,  hidePostForm,isWebviewAllowJavascriptInterface);
        }


        Topic topic = createTopic(handler, context, id, mainMatcher.group(1));

        TopicBodyBuilder topicBodyBuilder = new TopicBodyBuilder(logined, topic, urlParams, enableSig,
                enableEmo, postBody,  hidePostForm,isWebviewAllowJavascriptInterface);
        topicBodyBuilder.beginTopic();

        final Pattern postPattern = Pattern.compile("<a name=\"entry(\\d+)\"></a><div class=\"post_header_container\">([\\s\\S]*?)((<!--Begin Msg Number)|(<!-- TABLE))", Pattern.MULTILINE);
        final Pattern postHeaderPattern = Pattern.compile("<span class=\"post_date\">(.*?)&nbsp;.*?#(\\d+).*");
        final Pattern nickPattern = Pattern.compile("title=\"Вставить ник\" onclick=\"return insertText\\('\\[b\\](.*?),\\[/b\\]");
        final Pattern userInfoPattern = Pattern.compile("<span class=\"post_user_info\">(<strong>.*?</strong><br />)?Группа:(.*?)<font color=\"(.*?)\">.*?MID=(\\d+)\">PM</a>");
        final Pattern reputationPattern = Pattern.compile("title=\"Просмотреть репутацию\">(.*?)</a>");
        final Pattern actionsPattern = Pattern.compile(".*Жалоба.*");
        final Pattern bodyPattern = Pattern.compile("<div class=\"post_body\">([\\s\\S]*)</div>");


        Matcher matcher = postPattern.matcher(mainMatcher.group(2) + "<!-- TABLE");
        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();
        Post post = null;
        Boolean spoil = spoilFirstPost;
        while (matcher.find()) {
            if (post != null) {
                topicBodyBuilder.addPost(post, spoil);
                spoil = false;
            }
            String postId = matcher.group(1);

            String str = matcher.group(2);
            Matcher m = postHeaderPattern.matcher(str);
            if (m.find()) {
                post = new Post(postId, Functions.getForumDateTime(Functions.parseForumDateTime(m.group(1), today, yesterday)), m.group(2));

            } else
                continue;

            m = nickPattern.matcher(str);
            if (m.find()) {
                post.setAuthor(m.group(1));

            }

            m = userInfoPattern.matcher(str);
            if (m.find()) {
                post.setUserGroup(Html.fromHtml(m.group(2)).toString().trim());
                post.setUserState(m.group(3));
                post.setUserId(m.group(4));

            }

            m = reputationPattern.matcher(str);
            if (m.find()) {
                post.setUserReputation(m.group(1));
                post.setCanPlusRep(str.contains("Поднять репутацию"));
                post.setCanMinusRep(str.contains("Опустить репутацию"));

            }


            m = actionsPattern.matcher(str);
            if (m.find()) {
                post.setCanEdit(str.contains("Ред."));
                post.setCanDelete(str.contains("Удал."));

            }


            m = bodyPattern.matcher(str);
            if (m.find()) {
                post.setBody("<div class=\"post_body\">" + m.group(1) + "</div>");
            }

            //topic.addMessage(post);
        }
        if (post != null) {
            topicBodyBuilder.addPost(post, spoil);
            spoil = false;
        }
        topicBodyBuilder.endTopic();

        return topicBodyBuilder;
    }

    private static void beforeThemeMessages(Topic topic, Pattern titlePattern, Pattern moderatorTitlePattern, Pattern pagesCountPattern, Pattern lastPageStartPattern, Pattern currentPagePattern, String str) {
        Matcher m;
        if (topic.getPagesCount() == 1) {
            m = pagesCountPattern.matcher(str);
            if (m.find()) {
                topic.setPagesCount(m.group(1));
            }
        }
        if (topic.getLastPageStartCount() == 0) {
            m = lastPageStartPattern.matcher(str);
            while (m.find()) {
                topic.setLastPageStartCount(m.group(2));
            }
        }
        if (topic.getCurrentPage() == 0) {
            m = currentPagePattern.matcher(str);
            if (m.find()) {
                topic.setCurrentPage(m.group(1));
            }
        }
        if (TextUtils.isEmpty(topic.getTitle())) {
            m = titlePattern.matcher(str);
            if (m.find()) {
                topic.setTitle(m.group(1));
            } else {
                m = moderatorTitlePattern.matcher(str);
                if (m.find())
                    topic.setTitle(m.group(1));
            }
        }
    }

    public String getThemeForumId(String themeId) throws IOException {

        String res = performGet("http://4pda.ru/forum/lofiversion/index.php?t" + themeId + ".html");

        Pattern pattern = Pattern.compile("<div class='ipbnav'>.*<a href='http://4pda.ru/forum/lofiversion/index.php\\?f(\\d+).html'>.*?</a></div>", Pattern.MULTILINE);
        Matcher m = pattern.matcher(res);
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }

    }

    private void setThemeForumAndAuthKey(Topic topic) throws IOException {

        String res = performGet("http://4pda.ru/forum/lofiversion/index.php?t" + topic.getId() + ".html");

        if (TextUtils.isEmpty(topic.getForumId())) {
            Pattern pattern = Pattern.compile("<div class='ipbnav'>.*<a href='http://4pda.ru/forum/lofiversion/index.php\\?f(\\d+).html'>.*?</a></div>", Pattern.MULTILINE);
            Matcher m = pattern.matcher(res);
            if (m.find()) {
                topic.setForumId(m.group(1));
            }
        }

        if (TextUtils.isEmpty(topic.getAuthKey())) {
            Pattern pattern = Pattern.compile("name=\"auth_key\" value=\"(.*)\"", Pattern.MULTILINE);
            Matcher m = pattern.matcher(res);
            if (m.find()) {
                topic.setAuthKey(m.group(1));
            }
        }

    }

    public String addToFavorites(Topic topic) throws IOException {
        if (TextUtils.isEmpty(topic.getForumId())) {
            topic.setForumId(getThemeForumId(topic.getId()));
        }
        if (TextUtils.isEmpty(topic.getForumId())) {
            return "Не могу получить идентификатор форума для темы";
        }
        String res = performGet("http://4pda.ru/forum/index.php?autocom=favtopics&CODE=03&f=" + topic.getForumId() + "&t=" + topic.getId() + "&st=0");

        Pattern pattern = Pattern.compile("\\s*<div class=\"tablepad\">\\s*(.*)\\s*<ul>", Pattern.MULTILINE);
        Matcher m = pattern.matcher(res);
        if (m.find()) {
            return m.group(1);
        } else {
            pattern = Pattern.compile("\\s*<h4>Причина:</h4>\\s*<p>(.*?)</p>", Pattern.MULTILINE);
            m = pattern.matcher(res);
            if (m.find()) {
                return m.group(1);
            }
        }
        return "Результат неизвестен. Сообщите разработчику";
    }

    public String removeFromFavorites(Topic topic) throws IOException {
        if (TextUtils.isEmpty(topic.getForumId())) {
            topic.setForumId(getThemeForumId(topic.getId()));
        }
        if (TextUtils.isEmpty(topic.getForumId())) {
            return "Не могу получить идентификатор форума для темы";
        }
        performGet("http://4pda.ru/forum/index.php?autocom=favtopics&CODE=02&selectedtids=" + topic.getId() + "&cb=1&f=" + topic.getForumId() + "&t=" + topic.getId() + "&st=0");

        return "Выбранная Вами тема удалена из избранного";
    }


    public DownloadTask downloadFile(String url, OnProgressPositionChangedListener progressChangedListener) {
        DownloadTask downloadTask = m_DownloadTasks.add(url, progressChangedListener);
        HttpHelper httpHelper = new HttpHelper();

        try {
            httpHelper.downloadFile(downloadTask);
        } finally {
            httpHelper.close();
        }
        return downloadTask;
    }

    private DownloadTasks m_DownloadTasks = new DownloadTasks();

    public DownloadTasks getDownloadTasks() {
        return m_DownloadTasks;
    }


}
