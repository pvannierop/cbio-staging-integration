/*
* Copyright (c) 2018 The Hyve B.V.
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.example.demo.exceptions;

public class RestarterException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = -5072243529310767969L;

	public RestarterException()
	{
	}
	public RestarterException(String message, Exception e)
	{
		super(message, e);
	}
	public RestarterException(String message)
	{
		super(message);
	}

}
