package com.osell.activity.chat;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.osell.R;
import com.osell.activity.O2OProfileActivity;
import com.osell.activity.cominfo.CompInfoActivity;
import com.osell.app.OsellCenter;
import com.osell.db.DBHelper;
import com.osell.db.UserTable;
import com.osell.entity.Login;
import com.osell.entity.chat.ProductChat;
import com.osell.entity.home.ResponseData;
import com.osell.global.OSellCommon;
import com.osell.net.RestAPI;
import com.osell.util.ImageOptionsBuilder;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by wyj on 2016/9/27
 * 邀请供应商 生成三方聊天 及三方聊天相关
 */
public class ChatMainTreeChat {
    private ChatMainActivity context;

    private static ChatMainTreeChat chatMainTreeChat;

    private OsellCenter center = OsellCenter.getInstance();
    private int isRoom;
    private Login fCustomerVo;
    private ProductChat productChat;


    private ChatMainTreeChat(ChatMainActivity activity, int isRoom, Login fCustomerVo) {
        this.context = activity;
        this.isRoom = isRoom;
        this.fCustomerVo = fCustomerVo;

    }


    public static ChatMainTreeChat getInstance(ChatMainActivity activity, int isRoom, Login fCustomerVo) {
        if (null == chatMainTreeChat) {
            chatMainTreeChat = new ChatMainTreeChat(activity, isRoom, fCustomerVo);
        } else {
            chatMainTreeChat.setContext(activity);
            chatMainTreeChat.setIsRoom(isRoom);
            chatMainTreeChat.setfCustomerVo(fCustomerVo);

        }
        chatMainTreeChat.productChat = null;

        return chatMainTreeChat;
    }

    public static ChatMainTreeChat getCurrcyInstance() {
        if (chatMainTreeChat != null) return chatMainTreeChat;
        else return null;
    }


    public void setContext(ChatMainActivity context) {
        this.context = context;
    }

    public void setIsRoom(int isRoom) {
        this.isRoom = isRoom;
    }

    public void setfCustomerVo(Login fCustomerVo) {
        this.fCustomerVo = fCustomerVo;
    }

    public void clear(ChatMainActivity context) {
        if (null != chatMainTreeChat
                && (chatMainTreeChat.context == null || chatMainTreeChat.context == context)) {
            chatMainTreeChat = null;
        }
    }

    public void init() {
        if (isRoom == 0) {
//            initProductChat();
        } else {
            /**三方只有群聊了*/
            initThreeChat();
        }
    }


    /**
     * 初始化三方聊天
     */
    private void initThreeChat() {
        if (context == null) return;
        RestAPI.getInstance().oSellService().GetProductChat(fCustomerVo.uid)
                .subscribeOn(Schedulers.io())
                .map(new Func1<ResponseData<ProductChat>, ProductChat>() {
                    @Override
                    public ProductChat call(ResponseData<ProductChat> productChatResponseData) {

                        if (context == null) return null;


                        ProductChat pc = productChatResponseData.data;
                        productChat = pc;
                        if (pc == null) return null;
                        if (productChat.helperId.equals("0")
                                && productChat.salerId.equals("0")
                                && productChat.buyerId.equals("0")) {
                            return null;
                        }
                        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
                        UserTable usertable = new UserTable(db);
                        Login helper = null, saler = null, buyer = null;
                        if (Long.valueOf(pc.helperId) != 0) helper = usertable.query(pc.helperId);
                        if (Long.valueOf(pc.salerId) != 0) saler = usertable.query(pc.salerId);
                        if (Long.valueOf(pc.buyerId) != 0) buyer = usertable.query(pc.buyerId);
                        try {
                            if (saler == null) {
                                saler = OSellCommon.getOSellInfo().searchUserById(pc.salerId).mLogin;
                                usertable.insert(saler, -999);
                            }
                            if (buyer == null) {
                                buyer = OSellCommon.getOSellInfo().searchUserById(pc.buyerId).mLogin;
                                usertable.insert(buyer, -999);
                            }

                            if (helper == null && Long.valueOf(pc.helperId) != 0) {
                                helper = OSellCommon.getOSellInfo().searchUserById(pc.helperId).mLogin;
                                if (helper != null)
                                    usertable.insert(helper, -999);
                            }

                        } catch (Exception e) {

                            e.printStackTrace();
                        }


                        productChat.buyer = buyer;
                        productChat.helper = helper;
                        productChat.saler = saler;

                        return productChat;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ProductChat>() {
                               @Override
                               public void call(ProductChat productChat) {

                                   if (productChat != null) {


                                       context.findViewById(R.id.role_row).setVisibility(View.VISIBLE);

                                       View surLayout = context.findViewById(R.id.role_sup_layout);
                                       View salerLayout = context.findViewById(R.id.role_shop_layout);
                                       View buyerLayout = context.findViewById(R.id.role_buyer_layout);

                                       surLayout.setOnClickListener(listener);
                                       salerLayout.setOnClickListener(listener);
                                       buyerLayout.setOnClickListener(listener);

                                       ImageView surImg = (ImageView) context.findViewById(R.id.role_sup_img);
                                       ImageView salerImg = (ImageView) context.findViewById(R.id.role_shop_img);
                                       ImageView buyerImg = (ImageView) context.findViewById(R.id.role_buyer_img);

                                       TextView surText = (TextView) context.findViewById(R.id.role_sup_text);
                                       TextView salerText = (TextView) context.findViewById(R.id.role_shop_text);
                                       TextView buyerText = (TextView) context.findViewById(R.id.role_buyer_text);

                                       TextView surText2 = (TextView) context.findViewById(R.id.role_sup_text_2);
                                       TextView salerText2 = (TextView) context.findViewById(R.id.role_shop_text_2);
                                       TextView buyerText2 = (TextView) context.findViewById(R.id.role_buyer_text_2);

                                       if (productChat.helperId.equals("0")) {
                                           surLayout.setVisibility(View.GONE);
                                       }
                                       if (productChat.salerId.equals("0")) {
                                           salerLayout.setVisibility(View.GONE);
                                       }
                                       if (productChat.buyerId.equals("0")) {
                                           buyerLayout.setVisibility(View.GONE);
                                       }

                                       salerText.setText(productChat.salerRoleName);
                                       buyerText.setText(productChat.buyerRoleName);
                                       surText.setText(productChat.helperRoleName);

                                       salerText2.setText(productChat.salerName);
                                       buyerText2.setText(productChat.buyerName);
                                       surText2.setText(productChat.helperName);

                                       /**头像**/
                                       if (null != productChat.helper && Long.valueOf(productChat.helperId) != 0) {
                                           ImageLoader.getInstance().displayImage(productChat.helper.userFace, surImg, ImageOptionsBuilder.getInstance().getUserOptions());

                                       }
                                       if (null != productChat.buyer) {
                                           ImageLoader.getInstance().displayImage(productChat.buyer.userFace, buyerImg, ImageOptionsBuilder.getInstance().getUserOptions());
                                       }
                                       if (null != productChat.saler) {
                                           ImageLoader.getInstance().displayImage(productChat.saler.userFace, salerImg, ImageOptionsBuilder.getInstance().getUserOptions());
                                       }

                                       /**供应商被禁言**/
                                       if (productChat.noCanSay != null && productChat.noCanSay.contains(Integer.valueOf(productChat.helperId))) {
                                           context.findViewById(R.id.sur_forbid_icon).setVisibility(View.VISIBLE);
                                       } else {
                                           context.findViewById(R.id.sur_forbid_icon).setVisibility(View.GONE);
                                       }
                                       /**本人被禁言**/
                                       EditText editText = (EditText) context.findViewById(R.id.chat_box_edit_keyword);

                                       if (productChat.noCanSay != null
                                               && productChat.noCanSay.contains(Integer.valueOf(OSellCommon.getUid(context)))
                                               ) {
                                           context.findViewById(R.id.forbid_view).setVisibility(View.VISIBLE);
                                           context.findViewById(R.id.forbid_view).setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View view) {

                                               }
                                           });
                                           editText.setHint("");
                                       } else {
                                           context.findViewById(R.id.forbid_view).setVisibility(View.GONE);
                                           editText.setHint(context.getString(R.string.input_message_hint));
                                       }

                                   } else {
                                       context.findViewById(R.id.role_row).setVisibility(View.GONE);
                                   }
                               }
                           }
                        ,
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {

                            }
                        }
                );

    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String na = "";
            String id = "";


            switch (view.getId()) {
                case R.id.role_sup_layout:
                    id = productChat.helperId;
                    na = productChat.helperName;
                    break;
                case R.id.role_shop_layout:
                    id = productChat.salerId;
                    na = productChat.salerName;
                    break;
                case R.id.role_buyer_layout:
                    id = productChat.buyerId;
                    na = productChat.buyerName;
                    break;
            }

            SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
            UserTable usertable = new UserTable(db);
            Login login = usertable.query(id);

            if (!id.equals(OSellCommon.getUid(context))) {
                Intent intent = new Intent(context, CompInfoActivity.class);
                if (login != null) {
                    intent.putExtra("userid", login.userID);
                } else {
                    intent.putExtra("userName", na);
                }
                context.startActivity(intent);
            } else {
                context.startActivity(new Intent(context, O2OProfileActivity.class));
            }

        }
    };


    public ProductChat getProductChat() {
        return productChat;
    }
}
