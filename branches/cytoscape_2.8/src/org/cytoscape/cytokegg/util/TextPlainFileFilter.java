/*
 * Copyright (C) 2011-2012 Jos� Mar�a Villaveces Max Plank institute for biology
 * of ageing (MPI-age)
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.cytoscape.cytokegg.util;

import javax.activation.FileDataSource;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class TextPlainFileFilter extends FileFilter {

    public boolean accept(File file) {
        FileDataSource dataSource = new FileDataSource(file);

        if(dataSource.getContentType().equals("text/plain")){
            return true;
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Just text/plain mime type";
    }
}