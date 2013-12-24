/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tikal.hudson.plugins.notification;

import java.io.IOException;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.xstream.XStream;
import com.tikal.hudson.plugins.notification.model.JobState;
import com.tikal.hudson.plugins.notification.model.BuildState;
import com.tikal.hudson.plugins.notification.Phase;

public enum Format {
	SSTP {
		@Override
		protected byte[] serialize(JobState jobState) throws IOException {
            StringBuilder script = new StringBuilder();
            BuildState buildState = jobState.getBuild();
            Phase phase = buildState.getPhase();

            script.append("\\0\\_a[");
            script.append(jobState.getUrl());
            script.append("]");
            script.append(jobState.getName());
            script.append("\\_a");

            if (phase == Phase.STARTED) {
                script.insert(0, "\\s0");
                script.append("が開始しました。");
            } else {
                String status = buildState.getStatus();
                if ("SUCCESS".equals(status)) {
                    script.insert(0, "\\s0");
                    script.append("が成功しました。");
                } else if ("FAILURE".equals(status)) {
                    script.insert(0, "\\v\\s4\\f[height,18]…");
                    script.append("が失敗しました。\\w9\\s7\\![move,300,0,100,me]\\![move,-300,0,100,me]");
                    script.append("\\1\\f[height,18]直せ。\\n\\_a[");
                    script.append(buildState.getUrl());
                    script.append("]#");
                    script.append(buildState.getNumber());
                    script.append("\\_a");
                }
            }
            
            script.append("\\e");

            final String CRLF = "\r\n";
            StringBuilder s = new StringBuilder();
            s.append("NOTIFY SSTP/1.1").append(CRLF);
            s.append("Charset: UTF-8").append(CRLF);
            s.append("Sender: Jenkins-Server").append(CRLF); // FIXME: use machine name
            s.append("Script: ").append(script).append(CRLF);
            return s.toString().getBytes();
		}
	},
	XML {
		private XStream xstream = new XStream();

		@Override
		protected byte[] serialize(JobState jobState) throws IOException {
			xstream.processAnnotations(JobState.class);
			return xstream.toXML(jobState).getBytes();
		}
	},
	JSON {
		private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
		
		@Override
		protected byte[] serialize(JobState jobState) throws IOException {
			return gson.toJson(jobState).getBytes();
		}
	};
  
  abstract protected byte[] serialize(JobState jobState) throws IOException;
}
