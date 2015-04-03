package config;

public interface Const {
	/** 私聊广播 */
	public static final String ACTION_SINGLE_BROADCAST = "activity.ChatSingleAct";

	/** 群聊广播:在主界面 */
	public static final String ACTION_GROUP_MAIN = "group_main";

	/** 离线群组消息 */
	public static final String ACTION_GROUP_OFFLINE_MSG = "group_offline_msg";

	/** 群聊广播:在聊天界面 */
	public static final String ACTION_GROUP_CHAT = "group_chat";

	/** 上下线广播 */
	public static final String ACTION_ON_OR_OFF_LINE = "activity.HomeActivity";

	/** 发出创建聊天室的广播 */
	public static final String ACTION_CREATE_CHAT_ROOM = "create_chat_room";

	/** 发送广播到聊天列表界面，删除掉列表上显示的条数 */
	public static final String ACTION_DELETE_TIPS = "delete_tips";

	/** 加好友请求 */
	public static final String ACTION_ADDFRIEND_REQUEST = "add_friend_request";

	/** 同意添加好友请求 */
	public static final String ACTION_ADDFRIEND_REPONSE = "add_friend_response";
	
	/**断线重连通知*/
	public static final String ACTION_RECONNECT = "action_reconnect";

	/** 收到私聊消息 */
	public static final String ACTION_SINGLE_MSG = "single_msg";

	/** 获取好友列表成功后，发送通知，显示好友发给用户的离线消息 */
	public static final String ACTION_OFFLINE_MSG = "offline_msg";

	/** 群里界面新增好友，通知主界面更新列表 */
	public static final String ACTION_ADD_FRIENDTOCHAT = "addFriend2chatRoom";

	// public static final String BASE_URL =
	// "http://192.168.1.84:8080/IMServer/api/";
	public static final String BASE_URL = "http://170.240.100.124:8080/IMServer/api/";

	public static final String DB_FOLDER_NAME = "IM";

	public static final String DB__NAME = "IM.db";
	public static final String SP_NAME = "imclient";
	public static final String USER_NAME = "user_name";

	public static final String LOCAL__IMAGE_FOLDER = "IMImage";

	public static final String LOCAL__VOICE_FOLDER = "IMVoice";

	// public static final String NEETY_IP = "192.168.1.84";
	public static final String NEETY_IP = "170.240.100.124";

	public static final int NETTY_PORT = 9527;
}
