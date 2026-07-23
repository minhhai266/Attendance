def append_created_date_filters(clauses: list[str], params: list, date_from: str | None, date_to: str | None) -> None:
    if date_from:
        clauses.append("date(created_at) >= date(?)")
        params.append(date_from)
    if date_to:
        clauses.append("date(created_at) <= date(?)")
        params.append(date_to)


def append_attendance_date_filters(clauses: list[str], params: list, date_from: str | None, date_to: str | None) -> None:
    if date_from:
        clauses.append("date(attendance_date) >= date(?)")
        params.append(date_from)
    if date_to:
        clauses.append("date(attendance_date) <= date(?)")
        params.append(date_to)


def append_alert_event_date_filters(clauses: list[str], params: list, date_from: str | None, date_to: str | None) -> None:
    if date_from:
        clauses.append("date(COALESCE(event_date, created_at)) >= date(?)")
        params.append(date_from)
    if date_to:
        clauses.append("date(COALESCE(event_date, created_at)) <= date(?)")
        params.append(date_to)
