import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.mysql.jdbc.log.Log;

import model.Edad;
import model.Gse;
import model.MedicionCanal;
import model.MedicionHogar;
import model.MedicionIndividuo;

public class ImportMet {
	
	public static void main(String[] args) {
		
		// Inicio conexion a la base de datos usando Hibernate
		SessionFactory factory = new Configuration().configure().buildSessionFactory();
		Session session = factory.openSession();
		
		Path dirMets = Paths.get("mets/So_Dc");

		try {
			
			// Obtengo los archivos dentro del directorio
			DirectoryStream<Path> files = Files.newDirectoryStream(dirMets);
			
			for (Path file: files) {
				System.out.print(file.toString() + ": ");
				
				// Reviso si el archivo ya se encuentra en el log
				@SuppressWarnings("unchecked")
				List<model.Log> logs = (List<model.Log>) session.createQuery("FROM Log WHERE filename = :filename").setParameter("filename", file.getFileName().toString()).getResultList();
				
				if (logs.size() == 0) {

					// Leo archivo con datos
					List<String> lines = Files.readAllLines(file);

					String firstline = lines.get(0);
					if (!firstline.isEmpty()) {
						char token = firstline.charAt(0);
						if (token == 'I') {
							String inicioStr = firstline.substring(9, 21);
							Calendar fechaInicio = Calendar.getInstance();
							fechaInicio.set(
									Integer.valueOf(inicioStr.substring(0, 4)), 
									Integer.valueOf(inicioStr.substring(4, 6)), 
									Integer.valueOf(inicioStr.substring(6, 8)));

							@SuppressWarnings("unchecked")
							List<model.Log> logsByDate = session.createQuery("FROM Log WHERE fecha_archivo = :fecha_archivo").setParameter("fecha_archivo", fechaInicio.getTime()).getResultList();
						}
						else {
							
						}
					}
					else {
						
					}
					
					boolean detailData = false;
					Integer medicionHogarId = null;
					Integer medicionIndividuoId = null;
					Calendar fechaInicio = Calendar.getInstance();
					Calendar fechaTermino = Calendar.getInstance();

					// Inicio una transaccion SQL
					Transaction tx = session.beginTransaction();
					
					// Recorro cada linea
					for (String line: lines) {
						
						// Si la linea esta en blanco se salta a la siguiente
						if (line.length() == 0 || line.charAt(0) == '<') {
							continue;
						}
						
//						String hogarId;
						MedicionHogar hogar = null;
						MedicionIndividuo individuo = null;

						char token = line.charAt(0);
						switch (token) {
						
							// Informacion basica del hogar
							case 'I':
								
								// Se crean variables con strings de la linea
								detailData = false;
								String hogarId = line.substring(1, 9);
								String inicioStr = line.substring(9, 21);
								String terminoStr = line.substring(21, 33);

								fechaInicio.set(
										Integer.valueOf(inicioStr.substring(0, 4)), 
										Integer.valueOf(inicioStr.substring(4, 6)) - 1, 
										Integer.valueOf(inicioStr.substring(6, 8)),
										Integer.valueOf(inicioStr.substring(8, 10)),
										Integer.valueOf(inicioStr.substring(10)), 
										0);

								fechaTermino.set(
										Integer.valueOf(terminoStr.substring(0, 4)),
										Integer.valueOf(terminoStr.substring(4, 6)) - 1,
										Integer.valueOf(terminoStr.substring(6, 8)),
										Integer.valueOf(terminoStr.substring(8, 10)),
										Integer.valueOf(terminoStr.substring(10)),
										0);
								
								// Se insertan los datos del hogar en la base de datos
								hogar = new MedicionHogar();
								hogar.setHogarId(Integer.valueOf(hogarId));
								hogar.setFechaInicio(fechaInicio.getTime());
								hogar.setFechaTermino(fechaTermino.getTime());
								medicionHogarId = (Integer) session.save(hogar);
								
								break;
							
							// Informacion detallada del hogar o del individuo
							case 'D':
								String[] ids = line.split(",");
								
								// Informacion del individuo
								if (detailData) {
									if (ids[1].equals("0")) {
										break;
									}
									
									Gse gse = session.get(Gse.class, Integer.valueOf(ids[1]));
									Edad edad = session.get(Edad.class, Integer.valueOf(ids[6]));
									
									// Actualizo la informacion del individuo con el detalle
									individuo = session.get(MedicionIndividuo.class, medicionIndividuoId);
									individuo.setGse(gse);
									individuo.setEdad(edad);
									individuo.setCable(Integer.valueOf(ids[2]));
									individuo.setGenero(Integer.valueOf(ids[5]));
									individuo.setEdadCnt(Integer.valueOf(ids[7]));
									individuo.setTipoDuennaCasa(Integer.valueOf(ids[4]));
//									individuo.setTipoTrabaja(Integer.valueOf(ids[4]));
									individuo.setTipoJefeHogar(Integer.valueOf(ids[8]));
									session.update(individuo);

								}
								
								// Informacion del hogar
								else {
									Gse gse = session.get(Gse.class, Integer.valueOf(ids[1]));
									
									// Actualizo la informacion del hogar con el detalle
									hogar = session.get(MedicionHogar.class, medicionHogarId);
									hogar.setGse(gse);
									hogar.setCable(Integer.valueOf(ids[2]));
									session.update(hogar);
								}
								break;
							
							// Peso del hogar o el individuo
							case 'W':
								String peso = line.substring(1);
								
								// Informacion del individuo
								if (detailData) {
									individuo = session.get(MedicionIndividuo.class, medicionIndividuoId);
									individuo.setIndividuoPeso(new BigDecimal(peso));
									session.update(individuo);
								}
								
								// Informacion del hogar
								else {
									hogar = session.get(MedicionHogar.class, medicionHogarId);
									hogar.setHogarPeso(new BigDecimal(peso));
									session.update(hogar);
								}
								break;
							
							// Informacion basica del individuo
							case 'Z':
								detailData = true;

								String individuoId = line.substring(9);
								
								hogar = session.get(MedicionHogar.class, medicionHogarId);

								// Se insertan los datos del individuo en la base de datos
								individuo = new MedicionIndividuo();
								individuo.setMedicionHogar(hogar);
								individuo.setIndividuoId(Integer.valueOf(individuoId));
								medicionIndividuoId = (Integer) session.save(individuo);
								
								break;
							
							// Informacion de los canales vistos
							default:
								
								// Se crean variables con strings de la linea
								String tvId = line.substring(0, 2);
								String canalId = line.substring(2, 11);
								String minVistos = line.substring(12);
								
								Calendar fechaInicioCanal = (Calendar) fechaInicio.clone();
								
								individuo = session.get(MedicionIndividuo.class, medicionIndividuoId);
								
								// Expresion regular para ver minutos vistos por canal
								Pattern p = Pattern.compile("[A|B][0-9]+");
								Matcher m = p.matcher(minVistos);
								
								while (m.find()) {
									char tipo = m.group().charAt(0);
									String minutos = m.group().substring(1);
									
									if (tipo == 'B') {
										Calendar fechaTerminoCanal = (Calendar) fechaInicioCanal.clone();
										fechaTerminoCanal.add(Calendar.MINUTE, Integer.valueOf(minutos));

										// Se inserta la medicion del canal
										MedicionCanal canal = new MedicionCanal();								
										canal.setMedicionIndividuo(individuo);
										canal.setTelevisorId(Integer.valueOf(tvId));
										canal.setCanalId(Integer.valueOf(canalId));
										canal.setFechaInicio(fechaInicioCanal.getTime());
										canal.setFechaTermino(fechaTerminoCanal.getTime());
										session.save(canal);
									}
									
									// Sumo los minutos hasta el momento
									fechaInicioCanal.add(Calendar.MINUTE, Integer.valueOf(minutos));
								}
								break;
						}
						
					}

					// System.out.println(logs.size());
					
					model.Log log = new model.Log();
					log.setFilename(file.getFileName().toString());
					session.save(log);
					
					tx.commit();
					
					System.out.println("OK");
				}
				else {
					System.out.println("ALREADY EXIST");
				}
				
			}
		}
		catch (IOException e) {
			System.out.println("ERROR");
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.exit(0);
		
		
/*		Path file = Paths.get("mets/So_Dc/20161010.Met");
		
		try {
			

		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		// Se realiza cambios a la base de datos
//		tx.commit();
	}
}