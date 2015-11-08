package tools.server;

import com.orientechnologies.orient.server.config.OServerCommandConfiguration;
import com.orientechnologies.orient.server.network.protocol.http.OHttpRequest;
import com.orientechnologies.orient.server.network.protocol.http.OHttpResponse;
import com.orientechnologies.orient.server.network.protocol.http.OHttpUtils;
import com.orientechnologies.orient.server.network.protocol.http.command.OServerCommandAuthenticatedDbAbstract;

public class ImportHandler extends OServerCommandAuthenticatedDbAbstract
{

	public ImportHandler(final OServerCommandConfiguration iConfiguration)
	{

	}

	@Override
	public boolean execute(final OHttpRequest iRequest, OHttpResponse iResponse)
			throws Exception
	{

		String[] urlParts = checkSyntax(iRequest.url, 3,
				"Syntax error: hello/<database>/<name>");

		String name = urlParts[2];

		String result = "Hello " + name;

		iResponse.send(OHttpUtils.STATUS_OK_CODE, "OK", null,
				OHttpUtils.CONTENT_TEXT_PLAIN, result);

		return false;
	}

	@Override
	public String[] getNames()
	{
		return new String[] { "GET|importcode/*" };
	}
}
