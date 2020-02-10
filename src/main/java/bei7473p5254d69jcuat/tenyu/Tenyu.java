package bei7473p5254d69jcuat.tenyu;

import java.io.*;
import java.nio.channels.*;

import glb.*;
import glb.Glb.*;
import javafx.application.*;
import javafx.stage.*;

/**
 *
 * @author exceptiontenyu@gmail.com
 */
public class Tenyu extends Application implements GlbMemberDynamicState {
	//多重起動防止
	static FileLock lock;
	static FileOutputStream stream;
	static FileChannel channel;
	static String lockFilePath = ".tenyuAppLaunchLock";
	static {
		try {
			stream = new FileOutputStream(lockFilePath);
			channel = stream.getChannel();
			lock = channel.tryLock();
			if (lock == null || !lock.isValid()) {
				System.out.println("多重起動だったので終了");
				System.exit(1);
			} else {
				// 多重起動ではなかった
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage primaryStage) {
		Glb.setApp(this);
		Glb.getGui().build(primaryStage);
		Glb.startApplication();
	}

	public static void main(String[] args) {
		Glb.setupForStandard();
		if (args.length > 0 && args[0].equals("update")) {
			//アップデートによるプロセス起動
			Glb.getConf().setAfterUpdateLaunch(true);
		} else {
			//通常起動の場合、0に
			//アップデート起動の場合、前回の値を継続する
			//なのでtransientとかstart()とかは解決策にならない
			//通常起動かアップデートかを判別できるここで設定する必要がある
			Glb.getMiddle().getObjeCatchUp().setInitiallyCatchUpCount(0);
		}

		launch(args);
	}

	@Override
	public void stop() {
		Glb.getGui().setTitle(null, Lang.EXIT.toString());
		Glb.getGui().getPrimary().hide();
		Glb.debug("Tenyu#stop() is called.");
		Glb.stopApplication();
		try {
			super.stop();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}

		//多重起動防止ロックの解放
		Glb.debug("多重起動防止ロックの解放");
		try {
			lock.release();
			channel.close();
			stream.close();
			new File(lockFilePath).delete();
		} catch (IOException e) {
			Glb.getLogger().error("", e);
		}
		Glb.getLogger().info("終了しました。");
		Platform.exit();
		System.exit(0);//TODO こう書かないと終了しないがJavaFXの標準的な終了方法だろうか？
	}

}
