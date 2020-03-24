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
package org.olat.modules.quality.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.modules.quality.QualityDataCollectionStatus.FINISHED;
import static org.olat.modules.quality.QualityDataCollectionStatus.PREPARATION;
import static org.olat.modules.quality.QualityDataCollectionStatus.READY;
import static org.olat.modules.quality.QualityDataCollectionStatus.RUNNING;
import static org.olat.modules.quality.QualityReminderType.ANNOUNCEMENT_COACH_CONTEXT;
import static org.olat.modules.quality.QualityReminderType.ANNOUNCEMENT_COACH_TOPIC;
import static org.olat.modules.quality.QualityReminderType.INVITATION;
import static org.olat.modules.quality.QualityReminderType.REMINDER1;
import static org.olat.modules.quality.QualityReminderType.REMINDER2;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualityReminderType;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityReminderDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	
	@Autowired
	private QualityReminderDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateReminder() {
		QualityDataCollectionRef dataCollectionRef = qualityTestHelper.createDataCollection();
		Date sendDate = new Date();
		QualityReminderType type = QualityReminderType.REMINDER1;
		dbInstance.commitAndCloseSession();
		
		QualityReminder reminder = sut.create(dataCollectionRef, sendDate, type);
		
		assertThat(reminder).isNotNull();
		assertThat(reminder.getCreationDate()).isNotNull();
		assertThat(reminder.getLastModified()).isNotNull();
		assertThat(reminder.getType()).isEqualTo(type);
		assertThat(reminder.getSendPlaned()).isCloseTo(sendDate, 1000);
		assertThat(reminder.isSent()).isFalse();
		assertThat(reminder.getDataCollection().getKey()).isEqualTo(dataCollectionRef.getKey());
	}
	
	@Test
	public void shouldUpdateDatePlaned() {
		QualityReminder reminder = qualityTestHelper.createReminder();
		dbInstance.commitAndCloseSession();
		
		Date sendDate = (new GregorianCalendar(2013,1,28,13,24,56)).getTime();
		reminder = sut.updateDatePlaned(reminder, sendDate);

		assertThat(reminder.getSendPlaned()).isCloseTo(sendDate, 1000);
	}
	
	@Test
	public void shouldUpdateDateDone() {
		QualityReminder reminder = qualityTestHelper.createReminder();
		dbInstance.commitAndCloseSession();
		
		Date date = (new GregorianCalendar(2013,1,28,13,24,56)).getTime();
		reminder = sut.updateDateDone(reminder, date);

		assertThat(reminder.isSent()).isTrue();
	}
	
	@Test
	public void shouldLoadReminderByDataCollection() {
		Date sendDate = new Date();
		QualityDataCollectionRef dataCollectionRef = qualityTestHelper.createDataCollection();
		QualityReminder reminder1 = sut.create(dataCollectionRef, sendDate, QualityReminderType.ANNOUNCEMENT_COACH_TOPIC);
		QualityReminder reminder2 = sut.create(dataCollectionRef, sendDate, QualityReminderType.INVITATION);
		QualityReminder reminderOther = sut.create(qualityTestHelper.createDataCollection(), sendDate, QualityReminderType.INVITATION);
		dbInstance.commitAndCloseSession();
		
		List<QualityReminder> reminders = sut.load(dataCollectionRef);
		
		assertThat(reminders)
				.containsExactlyInAnyOrder(reminder1, reminder2)
				.doesNotContain(reminderOther);
	}
	
	@Test
	public void shouldLoadReminderByDataCollectionAndType() {
		Date sendDate = new Date();
		QualityDataCollectionRef dataCollectionRef = qualityTestHelper.createDataCollection();
		QualityReminderType type = QualityReminderType.REMINDER1;
		QualityReminder reminder = sut.create(dataCollectionRef, sendDate, type);
		sut.create(dataCollectionRef, sendDate, QualityReminderType.INVITATION);
		sut.create(qualityTestHelper.createDataCollection(), sendDate, type);
		dbInstance.commitAndCloseSession();
		
		QualityReminder loadedReminder = sut.load(dataCollectionRef, type);
		
		assertThat(loadedReminder).isEqualTo(reminder);
	}
	
	@Test
	public void shouldLoadPending() {
		Date until = (new GregorianCalendar(2013,1,28,1,1,1)).getTime();
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		qualityTestHelper.updateStatus(dataCollection, QualityDataCollectionStatus.RUNNING);
		QualityReminderType type = QualityReminderType.REMINDER1;
		Date afterUntil1 = (new GregorianCalendar(2013,1,28,2,1,1)).getTime();
		QualityReminder reminderAfter1 = sut.create(dataCollection, afterUntil1, type);
		Date afterUntil2 = (new GregorianCalendar(2013,1,28,3,1,1)).getTime();
		QualityReminder reminderAfter2 = sut.create(dataCollection, afterUntil2, type);
		Date beforeUntil1 = (new GregorianCalendar(2013,1,27,2,1,1)).getTime();
		QualityReminder reminderBefore1 = sut.create(dataCollection, beforeUntil1, type);
		Date beforeUntil2 = (new GregorianCalendar(2013,1,26,2,1,1)).getTime();
		QualityReminder reminderBefore2 = sut.create(dataCollection, beforeUntil2, type);
		Date beforeUntilSent = (new GregorianCalendar(2013,1,26,2,1,1)).getTime();
		QualityReminder reminderBeforeDone = sut.create(dataCollection, beforeUntilSent, type);
		reminderBeforeDone = sut.updateDateDone(reminderBeforeDone, beforeUntilSent);
		dbInstance.commitAndCloseSession();
		
		List<QualityReminder> pending = sut.loadPending(until);
		
		assertThat(pending).containsExactlyInAnyOrder(reminderBefore1, reminderBefore2)
				.doesNotContain(reminderAfter1, reminderAfter2, reminderBeforeDone);
	}
	
	@Test
	public void shouldLoadPendingDependingOnStatus() {
		Date until = (new GregorianCalendar(2013,1,28,1,1,1)).getTime();
		Date beforeUntil = (new GregorianCalendar(2013,1,26,2,1,1)).getTime();
		QualityReminder announcementCoachTopicPreparation = createDcReminder(beforeUntil, ANNOUNCEMENT_COACH_TOPIC, PREPARATION);
		QualityReminder announcementCoachTopicReady = createDcReminder(beforeUntil, ANNOUNCEMENT_COACH_TOPIC, READY);
		QualityReminder announcementCoachTopicRunning = createDcReminder(beforeUntil, ANNOUNCEMENT_COACH_TOPIC, RUNNING);
		QualityReminder announcementCoachTopicFinished = createDcReminder(beforeUntil, ANNOUNCEMENT_COACH_TOPIC, FINISHED);
		QualityReminder announcementCoachContextPreparation = createDcReminder(beforeUntil, ANNOUNCEMENT_COACH_CONTEXT, PREPARATION);
		QualityReminder announcementCoachContextReady = createDcReminder(beforeUntil, ANNOUNCEMENT_COACH_CONTEXT, READY);
		QualityReminder announcementCoachContextRunning = createDcReminder(beforeUntil, ANNOUNCEMENT_COACH_CONTEXT, RUNNING);
		QualityReminder announcementCoachContextFinished = createDcReminder(beforeUntil, ANNOUNCEMENT_COACH_CONTEXT, FINISHED);
		QualityReminder invitationPreparation = createDcReminder(beforeUntil, INVITATION, PREPARATION);
		QualityReminder invitationReady = createDcReminder(beforeUntil, INVITATION, READY);
		QualityReminder invitationRunning = createDcReminder(beforeUntil, INVITATION, RUNNING);
		QualityReminder invitationFinished = createDcReminder(beforeUntil, INVITATION, FINISHED);
		QualityReminder reminder1Preparation = createDcReminder(beforeUntil, REMINDER1, PREPARATION);
		QualityReminder reminder1Ready = createDcReminder(beforeUntil, REMINDER1, READY);
		QualityReminder reminder1Running = createDcReminder(beforeUntil, REMINDER1, RUNNING);
		QualityReminder reminder1Finished = createDcReminder(beforeUntil, REMINDER1, FINISHED);
		QualityReminder reminder2Preparation = createDcReminder(beforeUntil, REMINDER2, PREPARATION);
		QualityReminder reminder2Ready = createDcReminder(beforeUntil, REMINDER2, READY);
		QualityReminder reminder2Running = createDcReminder(beforeUntil, REMINDER2, RUNNING);
		QualityReminder reminder2Finished = createDcReminder(beforeUntil, REMINDER2, FINISHED);
		dbInstance.commitAndCloseSession();
		
		List<QualityReminder> pending = sut.loadPending(until);
		
		assertThat(pending)
				.containsExactlyInAnyOrder(
						announcementCoachTopicReady,
						announcementCoachContextReady,
						invitationRunning,
						reminder1Running,
						reminder2Running)
				.doesNotContain(
						announcementCoachTopicPreparation,
						announcementCoachTopicRunning,
						announcementCoachTopicFinished,
						announcementCoachContextPreparation, 
						announcementCoachContextRunning,
						announcementCoachContextFinished,
						invitationPreparation,
						invitationReady,
						invitationFinished,
						reminder1Preparation,
						reminder1Ready,
						reminder1Finished,
						reminder2Preparation,
						reminder2Preparation,
						reminder2Ready,
						reminder2Finished
				);
	}
	
	private QualityReminder createDcReminder(Date sendDate, QualityReminderType type, QualityDataCollectionStatus status) {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		qualityTestHelper.updateStatus(dataCollection, status);
		return sut.create(dataCollection, sendDate, type);
	}

	@Test
	public void shouldDeleteReminder() {
		Date sendDate = new Date();
		QualityDataCollectionRef dataCollectionRef = qualityTestHelper.createDataCollection();
		QualityReminderType type = QualityReminderType.REMINDER1;
		QualityReminder reminder = sut.create(dataCollectionRef, sendDate, type);
		QualityReminderType otherType = QualityReminderType.INVITATION;
		sut.create(dataCollectionRef, sendDate, otherType);
		dbInstance.commitAndCloseSession();
		
		sut.delete(reminder);
		dbInstance.commitAndCloseSession();
		
		QualityReminder loadedReminder = sut.load(dataCollectionRef, type);
		assertThat(loadedReminder).isNull();
		
		QualityReminder otherReminder = sut.load(dataCollectionRef, otherType);
		assertThat(otherReminder).isNotNull();
	}
	
	@Test
	public void shouldDeleteRemindersByDataCollection() {
		Date sendDate = new Date();
		QualityDataCollectionRef dataCollection = qualityTestHelper.createDataCollection();
		QualityDataCollectionRef dataCollectionOther = qualityTestHelper.createDataCollection();
		QualityReminderType type = QualityReminderType.REMINDER1;
		sut.create(dataCollection, sendDate, type);
		sut.create(dataCollectionOther, sendDate, type);
		dbInstance.commitAndCloseSession();
		
		sut.deleteReminders(dataCollection);
		dbInstance.commitAndCloseSession();
		
		QualityReminder loadedReminder = sut.load(dataCollection, type);
		assertThat(loadedReminder).isNull();
		
		QualityReminder otherReminder = sut.load(dataCollectionOther, type);
		assertThat(otherReminder).isNotNull();
	}

}
