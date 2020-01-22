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
package org.olat.course.editor.overview;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.editor.EditorMainController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.SelectEvent;
import org.olat.course.editor.overview.OverviewDataModel.OverviewCols;
import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.LearningPathTranslations;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OverviewListController extends FormBasicController implements FlexiTableCssDelegate {
	
	private static final String CMD_OPEN = "open";
	
	private FlexiTableElement tableEl;
	private OverviewDataModel dataModel;
	private FormLink bulkLink;

	private CloseableModalController cmc;
	private BulkChangeController bulkChangeCtrl;
	
	private final ICourse course;
	private final boolean learningPath;
	
	@Autowired
	private LearningPathService learningPathService;

	public OverviewListController(UserRequest ureq, WindowControl wControl, ICourse course) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(EditorMainController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(LearningPathNodeConfigController.class, getLocale(), getTranslator()));
		this.course = course;
		this.learningPath = LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initTable(ureq, formLayout);
		initButtons(formLayout);
	}

	private void initTable(UserRequest ureq, FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
		intendedNodeRenderer.setIndentationEnabled(false);
		FlexiCellRenderer nodeRenderer = new TreeNodeFlexiCellRenderer(intendedNodeRenderer, CMD_OPEN);
		DefaultFlexiColumnModel nodeModel = new DefaultFlexiColumnModel(OverviewCols.node, CMD_OPEN, nodeRenderer);
		nodeModel.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(nodeModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.shortTitle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.longTitle));
		DefaultFlexiColumnModel learningObjectivesModel = new DefaultFlexiColumnModel(OverviewCols.learningObjectives);
		learningObjectivesModel.setCellRenderer(new TextFlexiCellRenderer(EscapeMode.none));
		learningObjectivesModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(learningObjectivesModel);
		DefaultFlexiColumnModel displayModel = new DefaultFlexiColumnModel(OverviewCols.display);
		displayModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(displayModel);
		
		if (learningPath) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.duration));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.obligation));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.start));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.trigger));
		}
		
		dataModel = new OverviewDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 250, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_course_edit_overview_table");
		tableEl.setCssDelegate(this);
		tableEl.setAndLoadPersistedPreferences(ureq, "course-editor-overview");
		tableEl.setEmtpyTableMessageKey("table.empty");
		tableEl.setMultiSelect(true);
		tableEl.setBordered(true);
		
		loadModel();
	}

	private void loadModel() {
		TreeNode rootNode = course.getEditorTreeModel().getRootNode();
		List<OverviewRow> rows = new ArrayList<>();
		forgeRows(rows, rootNode, 0, null);
		dataModel.setObjects(rows);
		tableEl.reset(true, false, true);
	}

	private void forgeRows(List<OverviewRow> rows, INode node, int recursionLevel, OverviewRow parent) {
		if (node instanceof CourseEditorTreeNode) {
			CourseEditorTreeNode editorNode = (CourseEditorTreeNode)node;
			OverviewRow row = forgeRow(editorNode, recursionLevel, parent);
			rows.add(row);
			
			int childCount = editorNode.getChildCount();
			for (int i = 0; i < childCount; i++) {
				INode child = editorNode.getChildAt(i);
				forgeRows(rows, child, ++recursionLevel, row);
			}
		}
	}

	private OverviewRow forgeRow(CourseEditorTreeNode editorNode, int recursionLevel, OverviewRow parent) {
		CourseNode courseNode = editorNode.getCourseNode();
		OverviewRow row = new OverviewRow(editorNode, recursionLevel);
		row.setParent(parent);
		row.setTranslatedDisplayOption(getTranslatedDisplayOption(courseNode));
		if (learningPath) {
			LearningPathConfigs learningPathConfigs = learningPathService.getConfigs(courseNode);
			row.setDuration(learningPathConfigs.getDuration());
			row.setTranslatedObligation(getTranslatedObligation(learningPathConfigs));
			row.setStart(learningPathConfigs.getStartDate());
			row.setTranslatedTrigger(getTranslatedTrigger(courseNode, learningPathConfigs));
		}
		return row;
	}

	private String getTranslatedDisplayOption(CourseNode courseNode) {
		String displayOption = courseNode.getDisplayOption();
		if (displayOption == null) return null;
		
		switch(displayOption) {
		case CourseNode.DISPLAY_OPTS_SHORT_TITLE_DESCRIPTION_CONTENT: return translate("nodeConfigForm.short_title_desc_content");
		case CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT: return translate("nodeConfigForm.title_desc_content");
		case CourseNode.DISPLAY_OPTS_SHORT_TITLE_CONTENT: return translate("nodeConfigForm.short_title_content");
		case CourseNode.DISPLAY_OPTS_TITLE_CONTENT: return translate("nodeConfigForm.title_content");
		case CourseNode.DISPLAY_OPTS_CONTENT: return translate("nodeConfigForm.content_only");
		default:
			// nothing
		}
		return null;
	}

	private String getTranslatedObligation(LearningPathConfigs learningPathConfigs) {
		AssessmentObligation obligation = learningPathConfigs.getObligation();
		if (obligation == null) return null;
		
		switch (obligation) {
		case mandatory: return translate("config.obligation.mandatory");
		case optional: return translate("config.obligation.optional");
		default:
			// nothing
		}
		return null;
	}

	private String getTranslatedTrigger(CourseNode courseNode, LearningPathConfigs learningPathConfigs) {
		FullyAssessedTrigger trigger = learningPathConfigs.getFullyAssessedTrigger();
		if (trigger == null) return null;
		
		switch (trigger) {
		case nodeVisited: return translate("config.trigger.visited");
		case confirmed: return translate("config.trigger.confirmed");
		case score: {
			Integer scoreTriggerValue = learningPathConfigs.getScoreTriggerValue();
			return translate("config.trigger.score.value", new String[] { scoreTriggerValue.toString() } );
		}
		case passed: return translate("config.trigger.passed");
		case statusInReview: {
			LearningPathTranslations translations = learningPathService.getEditConfigs(courseNode).getTranslations();
			return translations.getTriggerStatusInReview(getLocale()) != null
					? translations.getTriggerStatusInReview(getLocale())
					: translate("config.trigger.status.in.review");
		}
		case statusDone: {
			LearningPathTranslations translations = learningPathService.getEditConfigs(courseNode).getTranslations();
			return translations.getTriggerStatusDone(getLocale()) != null
					? translations.getTriggerStatusDone(getLocale())
					: translate("config.trigger.status.done");
		}
		default:
			// nothing
		}
		return null;
	}

	private void initButtons(FormItemContainer formLayout) {
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setElementCssClass("o_button_group");
		buttonCont.setRootForm(mainForm);
		formLayout.add("buttons", buttonCont);
		
		bulkLink = uifactory.addFormLink("command.bulk", buttonCont, Link.BUTTON);
	}
	
	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		return dataModel.getObject(pos).getEditorNode().getCssClass();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == bulkChangeCtrl) {
			if (event == FormEvent.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(bulkChangeCtrl);
		removeAsListenerAndDispose(cmc);
		bulkChangeCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				OverviewRow row = dataModel.getObject(se.getIndex());
				if (CMD_OPEN.equals(cmd)) {
					fireEvent(ureq, new SelectEvent(row.getCourseNode()));
				}
			}
		} else if (source == bulkLink) {
			doBulk(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doBulk(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex.isEmpty()) {
			showWarning("error.select.one.course.node");
			return;
		}
		
		List<CourseNode> selectedCourseNodes = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.map(OverviewRow::getCourseNode)
				.collect(Collectors.toList());
		
		removeAsListenerAndDispose(bulkChangeCtrl);
		bulkChangeCtrl = new BulkChangeController(ureq, getWindowControl(), course, selectedCourseNodes);
		listenTo(bulkChangeCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), bulkChangeCtrl.getInitialComponent(),
				true, translate("command.bulk"));
		cmc.activate();
		listenTo(cmc);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
