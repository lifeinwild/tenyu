package bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.urlprovement;

import java.net.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.server.urlprovementserver.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.useredge.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;

/**
 * URL証明
 *
 * この界隈はクラス名等にWebとURLのどちらかを使っている。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class URLProvementServer
		extends TakeOverServer<TakeOverMessageURLProvementServer> {
	@Override
	public void registerToOnlineChecker() {
		Glb.getMiddle().getOnlineChecker().register(getModuleName(),
				new OnlineCheckerFuncs(() -> checkAndStartOrStop(),
						() -> getServerCandidates()));
	}

	@Override
	public void takeover(TakeOverMessageURLProvementServer message) {
		//このサーバーは引き継ぐ状態が無い
	}

	@Override
	public List<NodeIdentifierUser> getServerCandidates() {
		return Glb.getObje()
				.getRole(rs -> rs.getByName(getModuleName()).getAdminNodes());
	}

	@Override
	public String getModuleName() {
		return URLProvementServer.class.getSimpleName();
	}

	@Override
	public boolean sendInheritingMessage(NodeIdentifierUser nextServer) {
		return TakeOverMessageURLProvementServer.send(nextServer);
	}

	/**
	 * URL証明
	 * @param webId	証明対象のWEBページ
	 * 予めWebオブジェクトを客観に登録しておく必要がある
	 */
	public boolean proveAndWrite(Long webId) {
		URLProvementInfoOnWeb info = prove(webId);
		if (info == null || !info.validate())
			return false;
		//証明成功時
		return sendMessage(info);
	}

	/**
	 * Web及びその社会性の管理者や登録者を変更するメッセージをネットワークに送信する
	 */
	private boolean sendMessage(URLProvementInfoOnWeb info) {
		Message resM = Glb.getP2p()
				.requestUserRightMessage(to -> new URLProved());

		if (Response.fail(resM))
			return false;
		Glb.getLogger().info("success");
		return true;
	}

	/**
	 * WebページにアクセスしてURL証明に関する情報を取得する
	 * @param webId	対象のWEBページ
	 * @return	取得された情報。検証される。検証に失敗した場合null
	 */
	private URLProvementInfoOnWeb prove(Long webId) {
		try {
			Web o = Glb.getObje().getWeb(ws -> ws.get(webId));
			if (o == null) {
				Glb.getLogger().error("web object is not found in db",
						new Exception());
				return null;
			}
			//get()はparseまでしてしまう。文字列が取得できればいいだけだから
			//必要ないかとも思ったが、文字コードの判定にmetaタグの情報を使用するので結局必要。
			Document doc = Jsoup.connect(o.getUrl()).get();
			Glb.getLogger().info(o.getUrl() + " " + doc.title());

			String adminArea = extractAdminArea(o.getUrl(), doc);
			if (adminArea == null)
				return null;

			String code = URLProvementInfoOnWeb.parseCode(adminArea);

			URLProvementInfoOnWeb r = URLProvementInfoOnWeb.create(code);
			if (r == null)
				return null;

			if (!r.validate()) {
				Glb.getLogger().warn("Failed to validate", new Exception());
				return null;
			}
			return r;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * HTMLからURL証明コードを抽出する
	 * @param doc
	 * @return
	 */
	private String extractAdminArea(String url, Document doc) {
		//URL証明コードを含んだWebページの管理者領域
		String adminArea;

		//headタグ
		adminArea = extractAdminAreaStandard(doc);
		if (adminArea != null) {
			return adminArea;
		}

		//サイト固有管理者領域
		adminArea = extractAdminAreaSiteSpecific(url, doc);
		if (adminArea != null) {
			return adminArea;
		}

		//文字列表現の取得に失敗した場合
		Glb.getLogger().warn("Failed to get adminArea. url=" + url,
				new Exception());
		return null;
	}

	/**
	 * headから探す
	 * @return	{@link URLProvementInfoOnWeb#toCode()}の返値
	 */
	private static String extractAdminAreaStandard(Document doc) {
		Elements metas = doc.head().getElementsByTag("meta");
		if (metas == null)
			return null;
		for (Element meta : metas) {
			String name = meta.attr("name");
			if (metaAttrName.equals(name)) {
				String content = meta.attr("content");
				if (content == null)
					continue;
				return content;
			}
		}
		return null;
	}

	/**
	 * metaタグのnameに指定する
	 */
	public static final String metaAttrName = "URLProvement";

	/**
	 * サイト固有の方法で管理者領域を抽出する
	 * @return	{@link URLProvementInfoOnWeb#toCode()}の返値
	 */
	private static String extractAdminAreaSiteSpecific(String url,
			Document doc) {
		try {
			return extractAdminAreaSiteSpecific(new URI(url), doc);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * https://stackoverflow.com/questions/9607903/get-domain-name-from-given-url
	 * URIを使うほうが良いらしいので
	 *
	 * @param url
	 * @param doc
	 * @return
	 */
	private static String extractAdminAreaSiteSpecific(URI url, Document doc) {
		String fqdn = url.getHost();
		String html = doc.html();
		if (fqdn == null || html == null)
			return null;

		return Glb.getObje().compute(txn -> {
			try {
				URLProvementRegexStore s = new URLProvementRegexStore(txn);
				List<URLProvementRegex> l = s.getByFqdn(fqdn);
				if (l == null || l.size() == 0)
					return null;

				for (URLProvementRegex r : l) {
					if (r.isSupport(fqdn)) {
						return r.parseAdminArea(html);
					}
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
			return null;
		});

	}
}
