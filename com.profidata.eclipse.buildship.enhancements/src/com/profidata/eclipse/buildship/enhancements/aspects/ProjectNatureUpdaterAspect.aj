package com.profidata.eclipse.buildship.enhancements.aspects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;

import com.profidata.eclipse.project.model.ProjectConstants;


@SuppressWarnings("restriction")
public aspect ProjectNatureUpdaterAspect {
	/**
	 * Prevent gradle nature to be added to plugin project (plugin nature defined)
	 * 
	 * @param theNatureIds
	 */
	@SuppressAjWarnings("adviceDidNotMatch")
	void around(String[] theNatureIds):
          execution(public void org.eclipse.core.internal.resources.ProjectDescription.setNatureIds(String[])) &&
          args(theNatureIds) {
		proceed(validate(theNatureIds));
	}

	private String[] validate(String[] theNatureIds) {
		List<String> allNatureIds = new ArrayList<>(Arrays.asList(theNatureIds));

		if (allNatureIds.contains(ProjectConstants.PLUGIN_NATURE_ID) && allNatureIds.contains(GradleProjectNature.ID)) {
			allNatureIds.remove(GradleProjectNature.ID);

			return allNatureIds.toArray(new String[allNatureIds.size()]);
		}

		return theNatureIds;
	}
}
