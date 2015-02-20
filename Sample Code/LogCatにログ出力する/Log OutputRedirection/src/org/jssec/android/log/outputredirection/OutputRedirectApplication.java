/*
 * Copyright (C) 2012-2014 Japan Smartphone Security Association
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jssec.android.log.outputredirection;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import android.app.Application;

public class OutputRedirectApplication extends Application {

	// どこにも出力しないPrintStream
	private final PrintStream emptyStream = new PrintStream(new OutputStream() {
		public void write(int oneByte) throws IOException {
			// do nothing
		}
	});

	@Override
	public void onCreate() {
		// リリースビルド時にSystem.out/errをどこにも出力しないPrintStreamにリダイレクトする

		// System.out/errの本来のストリームを退避する
		PrintStream savedOut = System.out;
		PrintStream savedErr = System.err;

		// 一旦、System.out/errをどこにも出力しないPrintStreamにリダイレクトする
		System.setOut(emptyStream);
		System.setErr(emptyStream);

		// デバッグ時のみ本来のストリームに戻す(リリースビルドでは下記1行がProGuardにより削除される)
		resetStreams(savedOut, savedErr);
	}

	// リリース時はProGuardにより下記メソッドがまるごと削除される
	private void resetStreams(PrintStream savedOut, PrintStream savedErr) {
		System.setOut(savedOut);
		System.setErr(savedErr);
	}
}
