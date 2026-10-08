// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'database.dart';

// ignore_for_file: type=lint
class Course extends DataClass implements Insertable<Course> {
  final int id;
  final String name;
  final String code;
  final String instructor;
  final String room;
  final String colorHex;
  final String scheduleDays;
  final String startTime;
  final String endTime;
  final int targetAttendance;
  final String description;
  final DateTime createdAt;

  const Course({
    required this.id,
    required this.name,
    required this.code,
    required this.instructor,
    required this.room,
    required this.colorHex,
    required this.scheduleDays,
    required this.startTime,
    required this.endTime,
    required this.targetAttendance,
    required this.description,
    required this.createdAt,
  });

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['name'] = Variable<String>(name);
    map['code'] = Variable<String>(code);
    map['instructor'] = Variable<String>(instructor);
    map['room'] = Variable<String>(room);
    map['color_hex'] = Variable<String>(colorHex);
    map['schedule_days'] = Variable<String>(scheduleDays);
    map['start_time'] = Variable<String>(startTime);
    map['end_time'] = Variable<String>(endTime);
    map['target_attendance'] = Variable<int>(targetAttendance);
    map['description'] = Variable<String>(description);
    map['created_at'] = Variable<DateTime>(createdAt);
    return map;
  }

  CoursesCompanion toCompanion(bool nullToAbsent) {
    return CoursesCompanion(
      id: Value(id),
      name: Value(name),
      code: Value(code),
      instructor: Value(instructor),
      room: Value(room),
      colorHex: Value(colorHex),
      scheduleDays: Value(scheduleDays),
      startTime: Value(startTime),
      endTime: Value(endTime),
      targetAttendance: Value(targetAttendance),
      description: Value(description),
      createdAt: Value(createdAt),
    );
  }

  factory Course.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return Course(
      id: serializer.fromJson<int>(json['id']),
      name: serializer.fromJson<String>(json['name']),
      code: serializer.fromJson<String>(json['code']),
      instructor: serializer.fromJson<String>(json['instructor']),
      room: serializer.fromJson<String>(json['room']),
      colorHex: serializer.fromJson<String>(json['colorHex']),
      scheduleDays: serializer.fromJson<String>(json['scheduleDays']),
      startTime: serializer.fromJson<String>(json['startTime']),
      endTime: serializer.fromJson<String>(json['endTime']),
      targetAttendance: serializer.fromJson<int>(json['targetAttendance']),
      description: serializer.fromJson<String>(json['description']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
    );
  }

  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'name': serializer.toJson<String>(name),
      'code': serializer.toJson<String>(code),
      'instructor': serializer.toJson<String>(instructor),
      'room': serializer.toJson<String>(room),
      'colorHex': serializer.toJson<String>(colorHex),
      'scheduleDays': serializer.toJson<String>(scheduleDays),
      'startTime': serializer.toJson<String>(startTime),
      'endTime': serializer.toJson<String>(endTime),
      'targetAttendance': serializer.toJson<int>(targetAttendance),
      'description': serializer.toJson<String>(description),
      'createdAt': serializer.toJson<DateTime>(createdAt),
    };
  }

  Course copyWith({
    int? id,
    String? name,
    String? code,
    String? instructor,
    String? room,
    String? colorHex,
    String? scheduleDays,
    String? startTime,
    String? endTime,
    int? targetAttendance,
    String? description,
    DateTime? createdAt,
  }) =>
      Course(
        id: id ?? this.id,
        name: name ?? this.name,
        code: code ?? this.code,
        instructor: instructor ?? this.instructor,
        room: room ?? this.room,
        colorHex: colorHex ?? this.colorHex,
        scheduleDays: scheduleDays ?? this.scheduleDays,
        startTime: startTime ?? this.startTime,
        endTime: endTime ?? this.endTime,
        targetAttendance: targetAttendance ?? this.targetAttendance,
        description: description ?? this.description,
        createdAt: createdAt ?? this.createdAt,
      );
}

class CoursesCompanion extends UpdateCompanion<Course> {
  final Value<int> id;
  final Value<String> name;
  final Value<String> code;
  final Value<String> instructor;
  final Value<String> room;
  final Value<String> colorHex;
  final Value<String> scheduleDays;
  final Value<String> startTime;
  final Value<String> endTime;
  final Value<int> targetAttendance;
  final Value<String> description;
  final Value<DateTime> createdAt;

  const CoursesCompanion({
    this.id = const Value.absent(),
    this.name = const Value.absent(),
    this.code = const Value.absent(),
    this.instructor = const Value.absent(),
    this.room = const Value.absent(),
    this.colorHex = const Value.absent(),
    this.scheduleDays = const Value.absent(),
    this.startTime = const Value.absent(),
    this.endTime = const Value.absent(),
    this.targetAttendance = const Value.absent(),
    this.description = const Value.absent(),
    this.createdAt = const Value.absent(),
  });

  CoursesCompanion.insert({
    this.id = const Value.absent(),
    required String name,
    this.code = const Value.absent(),
    this.instructor = const Value.absent(),
    this.room = const Value.absent(),
    this.colorHex = const Value.absent(),
    this.scheduleDays = const Value.absent(),
    this.startTime = const Value.absent(),
    this.endTime = const Value.absent(),
    this.targetAttendance = const Value.absent(),
    this.description = const Value.absent(),
    this.createdAt = const Value.absent(),
  }) : name = Value(name);
}

class $CoursesTable extends Courses with TableInfo<$CoursesTable, Course> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $CoursesTable(this.attachedDatabase, [this._alias]);

  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
      'name', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  late final GeneratedColumn<String> code = GeneratedColumn<String>(
      'code', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  late final GeneratedColumn<String> instructor = GeneratedColumn<String>(
      'instructor', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  late final GeneratedColumn<String> room = GeneratedColumn<String>(
      'room', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  late final GeneratedColumn<String> colorHex = GeneratedColumn<String>(
      'color_hex', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('#4F46E5'));
  late final GeneratedColumn<String> scheduleDays = GeneratedColumn<String>(
      'schedule_days', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  late final GeneratedColumn<String> startTime = GeneratedColumn<String>(
      'start_time', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('09:00'));
  late final GeneratedColumn<String> endTime = GeneratedColumn<String>(
      'end_time', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('10:00'));
  late final GeneratedColumn<int> targetAttendance = GeneratedColumn<int>(
      'target_attendance', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(75));
  late final GeneratedColumn<String> description = GeneratedColumn<String>(
      'description', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
      'created_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);

  @override
  List<GeneratedColumn> get $columns => [
        id,
        name,
        code,
        instructor,
        room,
        colorHex,
        scheduleDays,
        startTime,
        endTime,
        targetAttendance,
        description,
        createdAt
      ];

  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'courses';

  @override
  Set<GeneratedColumn> get $primaryKey => {id};

  @override
  Course map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return Course(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      name: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}name'])!,
      code: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}code'])!,
      instructor: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}instructor'])!,
      room: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}room'])!,
      colorHex: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}color_hex'])!,
      scheduleDays: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}schedule_days'])!,
      startTime: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}start_time'])!,
      endTime: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}end_time'])!,
      targetAttendance: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}target_attendance'])!,
      description: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}description'])!,
      createdAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created_at'])!,
    );
  }

  @override
  $CoursesTable createAlias(String alias) => $CoursesTable(attachedDatabase, alias);
}

// Attendance Records
class AttendanceRecord extends DataClass implements Insertable<AttendanceRecord> {
  final int id;
  final int courseId;
  final int dateMillis;
  final String status;

  const AttendanceRecord({
    required this.id,
    required this.courseId,
    required this.dateMillis,
    required this.status,
  });

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['course_id'] = Variable<int>(courseId);
    map['date_millis'] = Variable<int>(dateMillis);
    map['status'] = Variable<String>(status);
    return map;
  }

  AttendanceRecordsCompanion toCompanion(bool nullToAbsent) {
    return AttendanceRecordsCompanion(
      id: Value(id),
      courseId: Value(courseId),
      dateMillis: Value(dateMillis),
      status: Value(status),
    );
  }

  factory AttendanceRecord.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return AttendanceRecord(
      id: serializer.fromJson<int>(json['id']),
      courseId: serializer.fromJson<int>(json['courseId']),
      dateMillis: serializer.fromJson<int>(json['dateMillis']),
      status: serializer.fromJson<String>(json['status']),
    );
  }

  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'courseId': serializer.toJson<int>(courseId),
      'dateMillis': serializer.toJson<int>(dateMillis),
      'status': serializer.toJson<String>(status),
    };
  }

  AttendanceRecord copyWith({
    int? id,
    int? courseId,
    int? dateMillis,
    String? status,
  }) =>
      AttendanceRecord(
        id: id ?? this.id,
        courseId: courseId ?? this.courseId,
        dateMillis: dateMillis ?? this.dateMillis,
        status: status ?? this.status,
      );
}

class AttendanceRecordsCompanion extends UpdateCompanion<AttendanceRecord> {
  final Value<int> id;
  final Value<int> courseId;
  final Value<int> dateMillis;
  final Value<String> status;

  const AttendanceRecordsCompanion({
    this.id = const Value.absent(),
    this.courseId = const Value.absent(),
    this.dateMillis = const Value.absent(),
    this.status = const Value.absent(),
  });

  AttendanceRecordsCompanion.insert({
    this.id = const Value.absent(),
    required int courseId,
    required int dateMillis,
    required String status,
  })  : courseId = Value(courseId),
        dateMillis = Value(dateMillis),
        status = Value(status);
}

class $AttendanceRecordsTable extends AttendanceRecords
    with TableInfo<$AttendanceRecordsTable, AttendanceRecord> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $AttendanceRecordsTable(this.attachedDatabase, [this._alias]);

  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  late final GeneratedColumn<int> courseId = GeneratedColumn<int>(
      'course_id', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: true,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'REFERENCES courses (id) ON DELETE CASCADE'));
  late final GeneratedColumn<int> dateMillis = GeneratedColumn<int>(
      'date_millis', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  late final GeneratedColumn<String> status = GeneratedColumn<String>(
      'status', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);

  @override
  List<GeneratedColumn> get $columns => [id, courseId, dateMillis, status];

  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'attendance_records';

  @override
  Set<GeneratedColumn> get $primaryKey => {id};

  @override
  AttendanceRecord map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return AttendanceRecord(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      courseId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}course_id'])!,
      dateMillis: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}date_millis'])!,
      status: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}status'])!,
    );
  }

  @override
  $AttendanceRecordsTable createAlias(String alias) =>
      $AttendanceRecordsTable(attachedDatabase, alias);
}

// Subjects
class Subject extends DataClass implements Insertable<Subject> {
  final int id;
  final String name;
  final String code;
  final String colorHex;
  final int? courseId;

  const Subject({
    required this.id,
    required this.name,
    required this.code,
    required this.colorHex,
    this.courseId,
  });

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['name'] = Variable<String>(name);
    map['code'] = Variable<String>(code);
    map['color_hex'] = Variable<String>(colorHex);
    if (!nullToAbsent || courseId != null) {
      map['course_id'] = Variable<int>(courseId);
    }
    return map;
  }

  SubjectsCompanion toCompanion(bool nullToAbsent) {
    return SubjectsCompanion(
      id: Value(id),
      name: Value(name),
      code: Value(code),
      colorHex: Value(colorHex),
      courseId: courseId == null && nullToAbsent
          ? const Value.absent()
          : Value(courseId),
    );
  }

  factory Subject.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return Subject(
      id: serializer.fromJson<int>(json['id']),
      name: serializer.fromJson<String>(json['name']),
      code: serializer.fromJson<String>(json['code']),
      colorHex: serializer.fromJson<String>(json['colorHex']),
      courseId: serializer.fromJson<int?>(json['courseId']),
    );
  }

  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'name': serializer.toJson<String>(name),
      'code': serializer.toJson<String>(code),
      'colorHex': serializer.toJson<String>(colorHex),
      'courseId': serializer.toJson<int?>(courseId),
    };
  }

  Subject copyWith({
    int? id,
    String? name,
    String? code,
    String? colorHex,
    int? courseId,
  }) =>
      Subject(
        id: id ?? this.id,
        name: name ?? this.name,
        code: code ?? this.code,
        colorHex: colorHex ?? this.colorHex,
        courseId: courseId ?? this.courseId,
      );
}

class SubjectsCompanion extends UpdateCompanion<Subject> {
  final Value<int> id;
  final Value<String> name;
  final Value<String> code;
  final Value<String> colorHex;
  final Value<int?> courseId;

  const SubjectsCompanion({
    this.id = const Value.absent(),
    this.name = const Value.absent(),
    this.code = const Value.absent(),
    this.colorHex = const Value.absent(),
    this.courseId = const Value.absent(),
  });

  SubjectsCompanion.insert({
    this.id = const Value.absent(),
    required String name,
    this.code = const Value.absent(),
    this.colorHex = const Value.absent(),
    this.courseId = const Value.absent(),
  }) : name = Value(name);
}

class $SubjectsTable extends Subjects with TableInfo<$SubjectsTable, Subject> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $SubjectsTable(this.attachedDatabase, [this._alias]);

  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
      'name', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  late final GeneratedColumn<String> code = GeneratedColumn<String>(
      'code', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  late final GeneratedColumn<String> colorHex = GeneratedColumn<String>(
      'color_hex', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('#06B6D4'));
  late final GeneratedColumn<int> courseId = GeneratedColumn<int>(
      'course_id', aliasedName, true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'REFERENCES courses (id) ON DELETE SET NULL'));

  @override
  List<GeneratedColumn> get $columns => [id, name, code, colorHex, courseId];

  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'subjects';

  @override
  Set<GeneratedColumn> get $primaryKey => {id};

  @override
  Subject map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return Subject(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      name: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}name'])!,
      code: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}code'])!,
      colorHex: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}color_hex'])!,
      courseId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}course_id']),
    );
  }

  @override
  $SubjectsTable createAlias(String alias) =>
      $SubjectsTable(attachedDatabase, alias);
}

// Chapters
class Chapter extends DataClass implements Insertable<Chapter> {
  final int id;
  final int subjectId;
  final String title;
  final int orderIndex;

  const Chapter({
    required this.id,
    required this.subjectId,
    required this.title,
    required this.orderIndex,
  });

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['subject_id'] = Variable<int>(subjectId);
    map['title'] = Variable<String>(title);
    map['order_index'] = Variable<int>(orderIndex);
    return map;
  }

  ChaptersCompanion toCompanion(bool nullToAbsent) {
    return ChaptersCompanion(
      id: Value(id),
      subjectId: Value(subjectId),
      title: Value(title),
      orderIndex: Value(orderIndex),
    );
  }

  factory Chapter.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return Chapter(
      id: serializer.fromJson<int>(json['id']),
      subjectId: serializer.fromJson<int>(json['subjectId']),
      title: serializer.fromJson<String>(json['title']),
      orderIndex: serializer.fromJson<int>(json['orderIndex']),
    );
  }

  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'subjectId': serializer.toJson<int>(subjectId),
      'title': serializer.toJson<String>(title),
      'orderIndex': serializer.toJson<int>(orderIndex),
    };
  }

  Chapter copyWith({
    int? id,
    int? subjectId,
    String? title,
    int? orderIndex,
  }) =>
      Chapter(
        id: id ?? this.id,
        subjectId: subjectId ?? this.subjectId,
        title: title ?? this.title,
        orderIndex: orderIndex ?? this.orderIndex,
      );
}

class ChaptersCompanion extends UpdateCompanion<Chapter> {
  final Value<int> id;
  final Value<int> subjectId;
  final Value<String> title;
  final Value<int> orderIndex;

  const ChaptersCompanion({
    this.id = const Value.absent(),
    this.subjectId = const Value.absent(),
    this.title = const Value.absent(),
    this.orderIndex = const Value.absent(),
  });

  ChaptersCompanion.insert({
    this.id = const Value.absent(),
    required int subjectId,
    required String title,
    this.orderIndex = const Value.absent(),
  })  : subjectId = Value(subjectId),
        title = Value(title);
}

class $ChaptersTable extends Chapters with TableInfo<$ChaptersTable, Chapter> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $ChaptersTable(this.attachedDatabase, [this._alias]);

  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  late final GeneratedColumn<int> subjectId = GeneratedColumn<int>(
      'subject_id', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: true,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'REFERENCES subjects (id) ON DELETE CASCADE'));
  late final GeneratedColumn<String> title = GeneratedColumn<String>(
      'title', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  late final GeneratedColumn<int> orderIndex = GeneratedColumn<int>(
      'order_index', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(0));

  @override
  List<GeneratedColumn> get $columns => [id, subjectId, title, orderIndex];

  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'chapters';

  @override
  Set<GeneratedColumn> get $primaryKey => {id};

  @override
  Chapter map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return Chapter(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      subjectId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}subject_id'])!,
      title: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}title'])!,
      orderIndex: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}order_index'])!,
    );
  }

  @override
  $ChaptersTable createAlias(String alias) =>
      $ChaptersTable(attachedDatabase, alias);
}

// Topics
class Topic extends DataClass implements Insertable<Topic> {
  final int id;
  final int chapterId;
  final String title;
  final bool isCompleted;
  final int orderIndex;
  final int? completedAtMillis;

  const Topic({
    required this.id,
    required this.chapterId,
    required this.title,
    required this.isCompleted,
    required this.orderIndex,
    this.completedAtMillis,
  });

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['chapter_id'] = Variable<int>(chapterId);
    map['title'] = Variable<String>(title);
    map['is_completed'] = Variable<bool>(isCompleted);
    map['order_index'] = Variable<int>(orderIndex);
    if (!nullToAbsent || completedAtMillis != null) {
      map['completed_at_millis'] = Variable<int>(completedAtMillis);
    }
    return map;
  }

  TopicsCompanion toCompanion(bool nullToAbsent) {
    return TopicsCompanion(
      id: Value(id),
      chapterId: Value(chapterId),
      title: Value(title),
      isCompleted: Value(isCompleted),
      orderIndex: Value(orderIndex),
      completedAtMillis: completedAtMillis == null && nullToAbsent
          ? const Value.absent()
          : Value(completedAtMillis),
    );
  }

  factory Topic.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return Topic(
      id: serializer.fromJson<int>(json['id']),
      chapterId: serializer.fromJson<int>(json['chapterId']),
      title: serializer.fromJson<String>(json['title']),
      isCompleted: serializer.fromJson<bool>(json['isCompleted']),
      orderIndex: serializer.fromJson<int>(json['orderIndex']),
      completedAtMillis: serializer.fromJson<int?>(json['completedAtMillis']),
    );
  }

  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'chapterId': serializer.toJson<int>(chapterId),
      'title': serializer.toJson<String>(title),
      'isCompleted': serializer.toJson<bool>(isCompleted),
      'orderIndex': serializer.toJson<int>(orderIndex),
      'completedAtMillis': serializer.toJson<int?>(completedAtMillis),
    };
  }

  Topic copyWith({
    int? id,
    int? chapterId,
    String? title,
    bool? isCompleted,
    int? orderIndex,
    int? completedAtMillis,
  }) =>
      Topic(
        id: id ?? this.id,
        chapterId: chapterId ?? this.chapterId,
        title: title ?? this.title,
        isCompleted: isCompleted ?? this.isCompleted,
        orderIndex: orderIndex ?? this.orderIndex,
        completedAtMillis: completedAtMillis ?? this.completedAtMillis,
      );
}

class TopicsCompanion extends UpdateCompanion<Topic> {
  final Value<int> id;
  final Value<int> chapterId;
  final Value<String> title;
  final Value<bool> isCompleted;
  final Value<int> orderIndex;
  final Value<int?> completedAtMillis;

  const TopicsCompanion({
    this.id = const Value.absent(),
    this.chapterId = const Value.absent(),
    this.title = const Value.absent(),
    this.isCompleted = const Value.absent(),
    this.orderIndex = const Value.absent(),
    this.completedAtMillis = const Value.absent(),
  });

  TopicsCompanion.insert({
    this.id = const Value.absent(),
    required int chapterId,
    required String title,
    this.isCompleted = const Value.absent(),
    this.orderIndex = const Value.absent(),
    this.completedAtMillis = const Value.absent(),
  })  : chapterId = Value(chapterId),
        title = Value(title);
}

class $TopicsTable extends Topics with TableInfo<$TopicsTable, Topic> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $TopicsTable(this.attachedDatabase, [this._alias]);

  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  late final GeneratedColumn<int> chapterId = GeneratedColumn<int>(
      'chapter_id', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: true,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'REFERENCES chapters (id) ON DELETE CASCADE'));
  late final GeneratedColumn<String> title = GeneratedColumn<String>(
      'title', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  late final GeneratedColumn<bool> isCompleted = GeneratedColumn<bool>(
      'is_completed', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'CHECK ("is_completed" IN (0, 1))'),
      defaultValue: const Constant(false));
  late final GeneratedColumn<int> orderIndex = GeneratedColumn<int>(
      'order_index', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(0));
  late final GeneratedColumn<int> completedAtMillis = GeneratedColumn<int>(
      'completed_at_millis', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);

  @override
  List<GeneratedColumn> get $columns =>
      [id, chapterId, title, isCompleted, orderIndex, completedAtMillis];

  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'topics';

  @override
  Set<GeneratedColumn> get $primaryKey => {id};

  @override
  Topic map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return Topic(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      chapterId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}chapter_id'])!,
      title: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}title'])!,
      isCompleted: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}is_completed'])!,
      orderIndex: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}order_index'])!,
      completedAtMillis: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}completed_at_millis']),
    );
  }

  @override
  $TopicsTable createAlias(String alias) =>
      $TopicsTable(attachedDatabase, alias);
}

// Tasks
class CourseTask extends DataClass implements Insertable<CourseTask> {
  final int id;
  final String title;
  final String description;
  final int? courseId;
  final int? subjectId;
  final int? topicId;
  final int? dueDateMillis;
  final int priority;
  final bool isCompleted;
  final int createdAtMillis;

  const CourseTask({
    required this.id,
    required this.title,
    required this.description,
    this.courseId,
    this.subjectId,
    this.topicId,
    this.dueDateMillis,
    required this.priority,
    required this.isCompleted,
    required this.createdAtMillis,
  });

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['title'] = Variable<String>(title);
    map['description'] = Variable<String>(description);
    if (!nullToAbsent || courseId != null) {
      map['course_id'] = Variable<int>(courseId);
    }
    if (!nullToAbsent || subjectId != null) {
      map['subject_id'] = Variable<int>(subjectId);
    }
    if (!nullToAbsent || topicId != null) {
      map['topic_id'] = Variable<int>(topicId);
    }
    if (!nullToAbsent || dueDateMillis != null) {
      map['due_date_millis'] = Variable<int>(dueDateMillis);
    }
    map['priority'] = Variable<int>(priority);
    map['is_completed'] = Variable<bool>(isCompleted);
    map['created_at_millis'] = Variable<int>(createdAtMillis);
    return map;
  }

  CourseTasksCompanion toCompanion(bool nullToAbsent) {
    return CourseTasksCompanion(
      id: Value(id),
      title: Value(title),
      description: Value(description),
      courseId: courseId == null && nullToAbsent
          ? const Value.absent()
          : Value(courseId),
      subjectId: subjectId == null && nullToAbsent
          ? const Value.absent()
          : Value(subjectId),
      topicId: topicId == null && nullToAbsent
          ? const Value.absent()
          : Value(topicId),
      dueDateMillis: dueDateMillis == null && nullToAbsent
          ? const Value.absent()
          : Value(dueDateMillis),
      priority: Value(priority),
      isCompleted: Value(isCompleted),
      createdAtMillis: Value(createdAtMillis),
    );
  }

  factory CourseTask.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return CourseTask(
      id: serializer.fromJson<int>(json['id']),
      title: serializer.fromJson<String>(json['title']),
      description: serializer.fromJson<String>(json['description']),
      courseId: serializer.fromJson<int?>(json['courseId']),
      subjectId: serializer.fromJson<int?>(json['subjectId']),
      topicId: serializer.fromJson<int?>(json['topicId']),
      dueDateMillis: serializer.fromJson<int?>(json['dueDateMillis']),
      priority: serializer.fromJson<int>(json['priority']),
      isCompleted: serializer.fromJson<bool>(json['isCompleted']),
      createdAtMillis: serializer.fromJson<int>(json['createdAtMillis']),
    );
  }

  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'title': serializer.toJson<String>(title),
      'description': serializer.toJson<String>(description),
      'courseId': serializer.toJson<int?>(courseId),
      'subjectId': serializer.toJson<int?>(subjectId),
      'topicId': serializer.toJson<int?>(topicId),
      'dueDateMillis': serializer.toJson<int?>(dueDateMillis),
      'priority': serializer.toJson<int>(priority),
      'isCompleted': serializer.toJson<bool>(isCompleted),
      'createdAtMillis': serializer.toJson<int>(createdAtMillis),
    };
  }

  CourseTask copyWith({
    int? id,
    String? title,
    String? description,
    int? courseId,
    int? subjectId,
    int? topicId,
    int? dueDateMillis,
    int? priority,
    bool? isCompleted,
    int? createdAtMillis,
  }) =>
      CourseTask(
        id: id ?? this.id,
        title: title ?? this.title,
        description: description ?? this.description,
        courseId: courseId ?? this.courseId,
        subjectId: subjectId ?? this.subjectId,
        topicId: topicId ?? this.topicId,
        dueDateMillis: dueDateMillis ?? this.dueDateMillis,
        priority: priority ?? this.priority,
        isCompleted: isCompleted ?? this.isCompleted,
        createdAtMillis: createdAtMillis ?? this.createdAtMillis,
      );
}

class CourseTasksCompanion extends UpdateCompanion<CourseTask> {
  final Value<int> id;
  final Value<String> title;
  final Value<String> description;
  final Value<int?> courseId;
  final Value<int?> subjectId;
  final Value<int?> topicId;
  final Value<int?> dueDateMillis;
  final Value<int> priority;
  final Value<bool> isCompleted;
  final Value<int> createdAtMillis;

  const CourseTasksCompanion({
    this.id = const Value.absent(),
    this.title = const Value.absent(),
    this.description = const Value.absent(),
    this.courseId = const Value.absent(),
    this.subjectId = const Value.absent(),
    this.topicId = const Value.absent(),
    this.dueDateMillis = const Value.absent(),
    this.priority = const Value.absent(),
    this.isCompleted = const Value.absent(),
    this.createdAtMillis = const Value.absent(),
  });

  CourseTasksCompanion.insert({
    this.id = const Value.absent(),
    required String title,
    this.description = const Value.absent(),
    this.courseId = const Value.absent(),
    this.subjectId = const Value.absent(),
    this.topicId = const Value.absent(),
    this.dueDateMillis = const Value.absent(),
    this.priority = const Value.absent(),
    this.isCompleted = const Value.absent(),
    required int createdAtMillis,
  })  : title = Value(title),
        createdAtMillis = Value(createdAtMillis);
}

class $CourseTasksTable extends CourseTasks
    with TableInfo<$CourseTasksTable, CourseTask> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $CourseTasksTable(this.attachedDatabase, [this._alias]);

  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  late final GeneratedColumn<String> title = GeneratedColumn<String>(
      'title', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  late final GeneratedColumn<String> description = GeneratedColumn<String>(
      'description', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  late final GeneratedColumn<int> courseId = GeneratedColumn<int>(
      'course_id', aliasedName, true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'REFERENCES courses (id) ON DELETE SET NULL'));
  late final GeneratedColumn<int> subjectId = GeneratedColumn<int>(
      'subject_id', aliasedName, true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'REFERENCES subjects (id) ON DELETE SET NULL'));
  late final GeneratedColumn<int> topicId = GeneratedColumn<int>(
      'topic_id', aliasedName, true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'REFERENCES topics (id) ON DELETE SET NULL'));
  late final GeneratedColumn<int> dueDateMillis = GeneratedColumn<int>(
      'due_date_millis', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  late final GeneratedColumn<int> priority = GeneratedColumn<int>(
      'priority', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(0));
  late final GeneratedColumn<bool> isCompleted = GeneratedColumn<bool>(
      'is_completed', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'CHECK ("is_completed" IN (0, 1))'),
      defaultValue: const Constant(false));
  late final GeneratedColumn<int> createdAtMillis = GeneratedColumn<int>(
      'created_at_millis', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);

  @override
  List<GeneratedColumn> get $columns => [
        id,
        title,
        description,
        courseId,
        subjectId,
        topicId,
        dueDateMillis,
        priority,
        isCompleted,
        createdAtMillis
      ];

  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'course_tasks';

  @override
  Set<GeneratedColumn> get $primaryKey => {id};

  @override
  CourseTask map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return CourseTask(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      title: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}title'])!,
      description: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}description'])!,
      courseId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}course_id']),
      subjectId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}subject_id']),
      topicId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}topic_id']),
      dueDateMillis: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}due_date_millis']),
      priority: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}priority'])!,
      isCompleted: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}is_completed'])!,
      createdAtMillis: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}created_at_millis'])!,
    );
  }

  @override
  $CourseTasksTable createAlias(String alias) =>
      $CourseTasksTable(attachedDatabase, alias);
}

abstract class _$AppDatabase extends GeneratedDatabase {
  _$AppDatabase(QueryExecutor e) : super(e);
  late final $CoursesTable courses = $CoursesTable(this);
  late final $AttendanceRecordsTable attendanceRecords =
      $AttendanceRecordsTable(this);
  late final $SubjectsTable subjects = $SubjectsTable(this);
  late final $ChaptersTable chapters = $ChaptersTable(this);
  late final $TopicsTable topics = $TopicsTable(this);
  late final $CourseTasksTable courseTasks = $CourseTasksTable(this);

  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities =>
      [courses, attendanceRecords, subjects, chapters, topics, courseTasks];
}
