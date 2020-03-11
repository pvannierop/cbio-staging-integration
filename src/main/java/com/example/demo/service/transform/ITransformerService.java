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
package com.example.demo.service.transform;

import java.util.List;

import com.example.demo.exceptions.TransformerException;
import com.example.demo.service.ExitStatus;

import org.springframework.core.io.Resource;

public interface ITransformerService {

    public List<String> parseCommandScript() throws TransformerException;

    public List<String> buildCommand(Resource untransformedFilesPath, Resource transformedFilesPath) throws TransformerException;

	public ExitStatus transform(Resource originPath, Resource finalPath, Resource logFile) throws TransformerException;

}
