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
package org.olat.modules.grading.model;

import java.math.BigDecimal;
import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingTimeRecord;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 10 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingAssignmentWithInfos {
	
	private boolean assessedIdentityVisible;
	private final Identity assessedIdentity;
	private final GradingAssignment assignment;
	private final AssessmentEntry assessmentEntry;
	private final RepositoryEntry referenceEntry;
	private final GradingTimeRecord timeRecord;
	private final GraderToIdentity grader;
	
	private String courseElementTitle;
	private String taxonomyLevels;
	
	public GradingAssignmentWithInfos(GradingAssignment assignment, GradingTimeRecord timeRecord, RepositoryEntry referenceEntry) {
		this.assignment = assignment;
		this.referenceEntry = referenceEntry;
		this.timeRecord = timeRecord;
		assessmentEntry = assignment.getAssessmentEntry();
		assessedIdentity = assessmentEntry.getIdentity();
		grader = assignment.getGrader();
		assessedIdentityVisible = false;
	}

	public RepositoryEntry getReferenceEntry() {
		return referenceEntry;
	}
	
	public String getSubIdent() {
		return assessmentEntry.getSubIdent();
	}
	
	public RepositoryEntry getEntry() {
		return assessmentEntry.getRepositoryEntry();
	}
	
	public GradingAssignment getAssignment() {
		return assignment;
	}
	
	public Identity getGrader() {
		return grader == null ? null : grader.getIdentity();
	}
	
	public AssessmentEntry getAssessmentEntry() {
		return assessmentEntry;
	}
	
	public GradingTimeRecord getTimeRecord() {
		return timeRecord;
	}
	
	public Long getTimeRecordedInSeconds() {
		return timeRecord == null ? null : timeRecord.getTime();
	}
	
	public Long getMetadataTimeRecordedInSeconds() {
		return timeRecord == null ? null : timeRecord.getMetadataTime();
	}
	
	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}
	
	public Date getAssessmentDate() {
		return assignment.getAssessmentDate();
	}
	
	public Boolean getPassed() {
		return assessmentEntry.getPassed();
	}
	
	public BigDecimal getScore() {
		return assessmentEntry.getScore();
	}

	public String getTaxonomyLevels() {
		return taxonomyLevels;
	}

	public void setTaxonomyLevels(String taxonomyLevels) {
		this.taxonomyLevels = taxonomyLevels;
	}

	public String getCourseElementTitle() {
		return courseElementTitle;
	}

	public void setCourseElementTitle(String courseElementTitle) {
		this.courseElementTitle = courseElementTitle;
	}

	public boolean isAssessedIdentityVisible() {
		return assessedIdentityVisible;
	}

	public void setAssessedIdentityVisible(boolean assessedIdentityVisible) {
		this.assessedIdentityVisible = assessedIdentityVisible;
	}

}
