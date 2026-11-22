#!/usr/bin/env python3

import argparse
from pathlib import Path


def parse_args() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser()
    parser.add_argument("--data", type=Path, required=True)
    parser.add_argument("--model", default="yolo11n.pt")
    parser.add_argument("--epochs", type=int, default=100)
    parser.add_argument("--imgsz", type=int, default=640)
    parser.add_argument("--batch", type=int, default=16)
    return parser


def main() -> int:
    args = parse_args().parse_args()
    try:
        from ultralytics import YOLO
    except ImportError as error:
        raise SystemExit(
            "Ultralytics is not installed. Use Python <= 3.12 in a separate "
            "training environment and install ultralytics."
        ) from error

    model = YOLO(args.model)
    model.train(
        data=str(args.data.resolve()),
        epochs=args.epochs,
        imgsz=args.imgsz,
        batch=args.batch,
        project=str((Path(__file__).resolve().parent / "data" / "runs").resolve()),
        name="coopnav-yolo11n",
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
