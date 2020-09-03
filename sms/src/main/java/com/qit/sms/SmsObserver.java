package com.qit.sms;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SmsObserver extends ContentObserver {

    public static final String TAG = "SMSObserver";

    private static final Uri CONTENT_URI = Uri.parse("content://sms");
    private static final String[] PROJECTION = new String[]{
            Sms._ID,//0
            Sms.TYPE,//1
            Sms.ADDRESS,//2
            Sms.BODY,//3
            Sms.DATE,//4
            Sms.THREAD_ID,//5
            Sms.READ,//6
    };

    private static final String SELECTION =
            Sms._ID + " > %s and (" + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_INBOX +
                    " or " + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_SENT +
                    " or " + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_OUTBOX + ")";

    private final static String QUERY_WHERE_CONDITION = Sms.READ + " = ? ";
    private final static String[] QUERY_WHERE_PARAMS = {" 0 "};

    private static final int COLUMN_INDEX_BODY = 3;
    private static final int COLUMN_INDEX_ID = 0;

    private static long mMaxId = 0;
    private ContentResolver mResolver;

    public SmsObserver(ContentResolver contentResolver, Handler handler) {
        super(handler);
        this.mResolver = contentResolver;
//        mMaxId = AccountController.getInstance().getMaxMessageId();
    }

    public void checkPermission() {
        mResolver.query(CONTENT_URI, PROJECTION,
                QUERY_WHERE_CONDITION, QUERY_WHERE_PARAMS, Sms._ID + " desc ");
    }

    @Override
    public void onChange(boolean selfChange) {
        Log.i(TAG, "onChange : " + selfChange + "; " + mMaxId + "; " + SELECTION);
        super.onChange(selfChange);

        Cursor cursor = null;
        try {
            cursor = mResolver.query(CONTENT_URI, PROJECTION,
                    QUERY_WHERE_CONDITION, QUERY_WHERE_PARAMS, Sms._ID + " desc ");

            while (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                String body = cursor.getString(COLUMN_INDEX_BODY);
                long id = cursor.getInt(COLUMN_INDEX_ID);
                if (id <= mMaxId) {
                    continue;
                }

                if (TextUtils.isEmpty(body)) {
                    continue;
                }

                mMaxId = id;
//                AccountController.getInstance().setMaxMessageID(mMaxId);

                String code = getDynamicVcode(body);
                if (TextUtils.isEmpty(code)) {
                    continue;
                }

//                EventSmsVcodeReceived event = new EventSmsVcodeReceived();
//                event.content = code;
//                EventCenter.getInstance().send(event);
                return;
            }
        } catch (SecurityException e) {
            Log.v(TAG, "权限错误，用户未授权读取短信信息！");
        } catch (IllegalArgumentException e) {
            Log.v(TAG, "参数错误，获取短信的查询参数错误！");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static final Pattern numberPattern = Pattern.compile("(\\d+)");

    /**
     * 从融360 的验证码短信中提取短信验证码
     */
    private String deriveVcode(String content) {
        Matcher matcher = numberPattern.matcher(content);
        while (matcher.find()) {
            int count = matcher.groupCount();
            for (int i = 0; i < count; i++) {
                String section = matcher.group(i);
                if (section.length() == 6) {
                    return section;
                }
            }
        }

        return "";
    }

    /**
     * 从字符串中截取连续6位数字组合 ([0-9]{" + 6 + "})截取六位数字 进行前后断言不能出现数字 用于从短信中获取动态密码
     *
     * @param str 短信内容
     * @return 截取得到的6位动态密码
     */
    public static String getDynamicVcode(String str) {
        // 6是验证码的位数一般为六位
        Pattern continuousNumberPattern = Pattern.compile("(?<![0-9])([0-9]{"
                + 6 + "})(?![0-9])");
        Matcher m = continuousNumberPattern.matcher(str);
        String dynamicPassword = "";
        while (m.find()) {
            dynamicPassword = m.group();
        }

        return dynamicPassword;
    }

    public interface Sms extends BaseColumns {
        String TYPE = "type";
        String THREAD_ID = "thread_id";
        String ADDRESS = "address";
        String DATE = "date";
        String READ = "read";
        String BODY = "body";
        int MESSAGE_TYPE_INBOX = 1;
        int MESSAGE_TYPE_SENT = 2;
        int MESSAGE_TYPE_OUTBOX = 4;
    }

}

