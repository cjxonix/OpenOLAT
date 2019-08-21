/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.lecture.ui.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.lecture.model.EditAbsenceNoticeWrapper;

/**
 * 
 * Initial date: 25 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNotice3LecturesEntriesStep extends BasicStep {
	
	private final boolean startPoint;
	private final EditAbsenceNoticeWrapper noticeWrapper;
	
	public AbsenceNotice3LecturesEntriesStep(UserRequest ureq) {
		this(ureq, null, false);
	}
	
	public AbsenceNotice3LecturesEntriesStep(UserRequest ureq, EditAbsenceNoticeWrapper noticeWrapper, boolean startPoint) {
		super(ureq);
		this.startPoint = startPoint;
		setNextStep(new AbsenceNotice4ReasonStep(ureq));
		if(startPoint) {
			this.noticeWrapper = noticeWrapper;
		} else {
			this.noticeWrapper = null;
		}
		setI18nTitleAndDescr("wizard.lecture.title", "wizard.lecture.title");
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(!startPoint, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		if(noticeWrapper != null) {
			runContext.put("absence", noticeWrapper);
		}
		return new DatesLecturesEntriesStepController(ureq, wControl, form, runContext);
	}
}