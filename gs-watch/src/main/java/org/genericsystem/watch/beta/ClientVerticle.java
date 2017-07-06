package org.genericsystem.watch.beta;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.streams.Pump;

public class ClientVerticle extends AbstractVerticle {

	public static void main(String[] args) {
		Runner.runExample(ClientVerticle.class);
	}

	@Override
	public void start() throws Exception {
		HttpClientRequest req = vertx.createHttpClient(new HttpClientOptions()).put(8080, "localhost", "/someurl",
				resp -> {
					System.out.println("Response " + resp.statusCode());
				});
		String filename = "upload.txt";
		FileSystem fs = vertx.fileSystem();

		fs.props(filename, ares -> {
			FileProps props = ares.result();
			System.out.println("props is " + props);
			long size = props.size();
			req.headers().set("content-length", "" + size);
			fs.open(filename, new OpenOptions(), ares2 -> {
				AsyncFile file = ares2.result();
				Pump pump = Pump.pump(file, req);
				file.endHandler(v -> {
					req.end();
				});
				pump.start();
			});
		});

	}
}