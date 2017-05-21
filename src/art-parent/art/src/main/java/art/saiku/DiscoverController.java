/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.saiku;

import art.servlets.Config;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import org.saiku.olap.discover.OlapMetaExplorer;
import org.saiku.olap.dto.SaikuCatalog;
import org.saiku.olap.dto.SaikuConnection;
import org.saiku.olap.dto.SaikuCube;
import org.saiku.olap.dto.SaikuCubeMetadata;
import org.saiku.olap.dto.SaikuDimension;
import org.saiku.olap.dto.SaikuMember;
import org.saiku.olap.dto.SaikuSchema;
import org.saiku.olap.util.exception.SaikuOlapException;
import org.saiku.service.olap.OlapDiscoverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/saiku2/rest/saiku/{username}/discover")
public class DiscoverController {

	private static final Logger logger = LoggerFactory.getLogger(DiscoverController.class);

	@GetMapping
	public List<SaikuConnection> discover() throws SaikuOlapException {
		List<SaikuConnection> connections = new ArrayList<>();

		List<SaikuCatalog> catalogs = new ArrayList<>();

		List<SaikuSchema> schemas = new ArrayList<>();

		List<SaikuCube> cubes = new ArrayList<>();

		SaikuCube cube = new SaikuCube("conn one", "unique cube name", "cube name", "cube caption", "catalog one", "schema one");
		cubes.add(cube);

		SaikuSchema schema = new SaikuSchema("schema one", cubes);
		schemas.add(schema);

		SaikuCatalog catalog = new SaikuCatalog("catalog one", schemas);
		catalogs.add(catalog);

		SaikuConnection conn = new SaikuConnection("conn one", catalogs);
		connections.add(conn);

		//return connections;
		//return Collections.emptyList();
		OlapDiscoverService olapDiscoverService = new OlapDiscoverService();
		OlapMetaExplorer metaExplorer = Config.getSaikuMetaExplorer();
		olapDiscoverService.setMetaExplorer(metaExplorer);
		
		return olapDiscoverService.getAllConnections();
	}

	@GetMapping("/{connection}")
	public List<SaikuConnection> getConnections(@PathVariable("connection") String connectionName)
			throws SaikuOlapException, SQLException {

		OlapDiscoverService olapDiscoverService = new OlapDiscoverService();
		OlapMetaExplorer metaExplorer = Config.getSaikuMetaExplorer();
		olapDiscoverService.setMetaExplorer(metaExplorer);
		
		return olapDiscoverService.getConnection(connectionName);
	}

	@GetMapping("/{connection}/{catalog}/{schema}/{cube}/metadata")
	public SaikuCubeMetadata getMetadata(
			@PathVariable("connection") String connectionName,
			@PathVariable("catalog") String catalogName,
			@PathVariable("schema") String schemaName,
			@PathVariable("cube") String cubeName) {

		if ("null".equals(schemaName)) {
			schemaName = "";
		}
		SaikuCube cube = new SaikuCube(connectionName, cubeName, cubeName, cubeName, catalogName, schemaName);

		OlapDiscoverService olapDiscoverService = new OlapDiscoverService();
		OlapMetaExplorer metaExplorer = Config.getSaikuMetaExplorer();
		olapDiscoverService.setMetaExplorer(metaExplorer);

		List<SaikuDimension> dimensions = olapDiscoverService.getAllDimensions(cube);
		List<SaikuMember> measures = olapDiscoverService.getMeasures(cube);
		Map<String, Object> properties = olapDiscoverService.getProperties(cube);
		return new SaikuCubeMetadata(dimensions, measures, properties);
	}
}
