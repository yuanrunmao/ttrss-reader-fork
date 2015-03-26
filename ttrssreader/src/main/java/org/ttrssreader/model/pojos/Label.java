/*
 * Copyright (c) 2015, Nils Braden
 *
 * This file is part of ttrss-reader-fork. This program is free software; you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; If
 * not, see http://www.gnu.org/licenses/.
 */

package org.ttrssreader.model.pojos;

import org.jetbrains.annotations.NotNull;

public class Label implements Comparable<Label> {

	public Integer id;
	public String caption;
	public boolean checked;
	public boolean checkedChanged = false;
	public String foregroundColor;
	public String backgroundColor;

	@Override
	public int compareTo(@NotNull Label l) {
		return id.compareTo(l.id);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Label) {
			Label other = (Label) o;
			return (id.equals(other.id));
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id + "".hashCode();
	}

	@Override
	public String toString() {
		return caption + ";" + foregroundColor + ";" + backgroundColor;
	}

}
